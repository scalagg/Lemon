package gg.scala.lemon.player

import gg.scala.common.Savable
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.config
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.internal.ExtHookIns
import gg.scala.lemon.minequest
import gg.scala.lemon.player.enums.PermissionCheck
import gg.scala.lemon.player.event.impl.RankChangeEvent
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategory.BAN
import gg.scala.lemon.player.punishment.category.PunishmentCategory.BLACKLIST
import gg.scala.lemon.player.punishment.category.PunishmentCategory.IP_RELATIVE
import gg.scala.lemon.player.punishment.category.PunishmentCategory.KICK
import gg.scala.lemon.player.punishment.category.PunishmentCategory.MUTE
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.GrantRecalculationUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.originalRank
import gg.scala.lemon.util.QuickAccess.realRank
import gg.scala.lemon.util.SplitUtil
import gg.scala.lemon.util.VaultUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.controller.annotations.Timestamp
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.time.DateUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.permissions.PermissionAttachment
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

class LemonPlayer(
    var uniqueId: UUID,

    @JvmField
    var ipAddress: String? = null,
    var firstLogin: Boolean = false
) : Savable, IDataStoreObject
{
    @JvmField
    @Timestamp
    var timestamp = 0L

    override val identifier: UUID
        get() = uniqueId

    val name: String
        get() = CubedCacheUtil.fetchName(identifier)!!

    var previousIpAddress: String? = null

    var pastIpAddresses = mutableMapOf<String, Long>()
    var pastLogins = mutableMapOf<String, Long>()

    val activePunishments =
        mutableMapOf<PunishmentCategory, Punishment?>()

    var assignedPermissions = mutableListOf<String>()
    var ignoring = mutableListOf<UUID>()

    private val handleOnConnection =
        mutableListOf<(Player) -> Unit>()

    private val lazyHandleOnConnection =
        mutableListOf<(Player) -> Unit>()

    var activeGrant: Grant? = null
    var activeSubGrant: Grant? = null

    private var attachment: PermissionAttachment? = null

    var metadata = mutableMapOf<String, Metadata>()
    var disguiseRankUniqueId: UUID? = null

    private val classInit = System
        .currentTimeMillis()

    val bukkitPlayer: Player?
        get() = Bukkit.getPlayer(uniqueId)

    init
    {
        for (value in PunishmentCategory.VALUES)
        {
            activePunishments[value] = null
        }
    }

    fun canInteract(player: LemonPlayer): Boolean
    {
        return !player.bukkitPlayer!!.hasMetadata("vanished") ||
                this.activeGrant!!.getRank().weight >= player.activeGrant!!.getRank().weight
    }

    fun disguiseRank() = this.disguiseRankUniqueId
        ?.let { RankHandler.findRank(it) }

    fun handleOnConnection(
        lambda: (Player) -> Unit
    )
    {
        this.handleOnConnection.add(lambda)
    }

    fun handleOnConnectionLazily(
        lambda: (Player) -> Unit
    )
    {
        this.lazyHandleOnConnection.add(lambda)
    }

    fun sortedPunishments() = activePunishments.entries
        .sortedByDescending { entry ->
            entry.value?.category?.ordinal ?: 0
        }

    fun recalculatePunishments(
        connecting: Boolean = false,
        nothing: Boolean = false
    ): CompletableFuture<Void>
    {
        val current = System.currentTimeMillis()

        return PunishmentHandler
            .fetchAllPunishmentsForTarget(uniqueId).thenAccept { list ->
                list.forEach { QuickAccess.attemptExpiration(it) }

                for (value in PunishmentCategory.VALUES)
                {
                    val newList = list.filter {
                        it.category == value && it.isActive
                    }

                    if (newList.isEmpty())
                    {
                        activePunishments[value] = null
                        continue
                    }

                    activePunishments[value] = newList[0]
                }

                if (nothing) return@thenAccept

                if (!connecting)
                {
                    activePunishments.forEach {
                        if (it.value != null)
                        {
                            val message = getPunishmentMessage(it.value!!)

                            when (it.value!!.category.intensity)
                            {
                                PunishmentCategoryIntensity.MEDIUM -> Tasks.sync {
                                    bukkitPlayer?.ifPresent { player ->
                                        player.kickPlayer(message)
                                    }
                                }

                                PunishmentCategoryIntensity.LIGHT -> bukkitPlayer
                                    ?.ifPresent { player ->
                                        player.sendMessage(message)
                                    }
                            }
                        }
                    }
                } else
                {
                    val sortedCategories = PunishmentCategory
                        .WEIGHTED_DENIED.sortedByDescending { it.ordinal }

                    for (sortedCategory in sortedCategories)
                    {
                        val punishmentInCategory = activePunishments[sortedCategory]

                        if (punishmentInCategory != null)
                        {
                            val message = getPunishmentMessage(punishmentInCategory)

                            lazyHandleOnConnection
                                .add { it.sendMessage(message) }

                            return@thenAccept
                        }
                    }
                }

                if (LemonConstants.DEBUG)
                {
                    println("[Lemon] It took ${System.currentTimeMillis() - current}ms to calculate punishments. ($name)")
                }
            }
    }

    fun getIpRelMessage(
        coloredName: String, punishment: Punishment
    ): String
    {
        if (punishment.category == BLACKLIST)
        {
            return String.format(
                Lemon.instance.languageConfig.blacklistRelationMessage,
                coloredName,
            )
        } else
        {
            return if (punishment.isPermanent)
            {
                String.format(
                    Lemon.instance.languageConfig.banRelationPermanentMessage,
                    coloredName, coloredName
                )
            } else
            {
                String.format(
                    Lemon.instance.languageConfig.banRelationTemporaryMessage,
                    coloredName, coloredName
                )
            }
        }
    }

    fun getPunishmentMessage(
        punishment: Punishment,
        current: Boolean = true
    ): String
    {
        return when (punishment.category)
        {
            KICK -> Lemon.instance.languageConfig.kickMessage
                .format(
                    Lemon.instance.settings.id,
                    punishment.addedReason
                )

            MUTE -> Lemon.instance.languageConfig.muteMessage
                .format(
                    if (current) "You've been" else "You're currently",
                    punishment.addedReason,
                    punishment.fancyDurationFromNowStringRaw
                )

            BAN -> if (punishment.isPermanent)
            {
                String.format(
                    Lemon.instance.languageConfig.permBanMessage,
                    punishment.addedReason,
                    SplitUtil.splitUuid(punishment.uuid)
                )
            } else
            {
                String.format(
                    Lemon.instance.languageConfig.tempBanMessage,
                    punishment.durationString,
                    punishment.addedReason,
                    SplitUtil.splitUuid(punishment.uuid)
                )
            }

            BLACKLIST -> Lemon.instance.languageConfig.blacklistMessage
            // already pre-handled
            IP_RELATIVE -> ""
        }
    }

    @JvmOverloads
    fun recalculateGrants(
        autoNotify: Boolean = false,
        forceRecalculatePermissions: Boolean = false,
        shouldCalculateNow: Boolean = false,
        connecting: Boolean = false
    ): CompletableFuture<Void>
    {
        if (Lemon.instance.settings.dummyServer)
        {
            return CompletableFuture
                .runAsync {
                    setupAutomaticGrant(null, register = false)
                }
        }

        return GrantHandler.fetchGrantsFor(uniqueId).thenAccept { grants ->
            if (grants == null || grants.isEmpty())
            {
                if (activeGrant != null)
                {
                    if (LemonConstants.DEBUG)
                    {
                        println("[Lemon] Skipping entity grant update for $name as their active grant is not-null.")
                    }
                } else
                {
                    setupAutomaticGrant(grants)
                }

                return@thenAccept
            }

            var shouldNotifyPlayer = autoNotify
            val previousRank = fetchPreviousRank(grants)

            grants.forEach { grant ->
                if (!grant.isRemoved && grant.hasExpired)
                {
                    grant.removedReason = "Expired"
                    grant.removedAt = System.currentTimeMillis()
                    grant.removedOn = Lemon.instance.settings.id

                    grant.save()
                }
            }

            run {
                val oldGrant = this.activeGrant

                this.activeGrant = GrantRecalculationUtil
                    .getProminentGrant(grants)

                // This should never happen, so we will
                // revert the grant back to the old grant.
                if (this.activeGrant == null && oldGrant != null)
                {
                    this.activeGrant = oldGrant
                    return@thenAccept
                }
            }

            var shouldRecalculatePermissions = forceRecalculatePermissions

            if (
                previousRank != null && activeGrant != null &&
                previousRank != activeGrant!!.getRank().uuid
            )
            {
                shouldRecalculatePermissions = true
                shouldNotifyPlayer = true
            }

            var subGrant: Grant? = null

            if (activeGrant != null)
            {
                subGrant = GrantRecalculationUtil
                    .getProminentSubGrant(
                        activeGrant!!, grants
                    )

                if (
                    subGrant != null && this.activeSubGrant?.uuid != subGrant.uuid &&
                    subGrant.getRank().uuid != RankHandler.getDefaultRank().uuid
                )
                {
                    shouldNotifyPlayer = true
                    shouldRecalculatePermissions = true
                }
            }

            if (shouldNotifyPlayer && !connecting)
            {
                bukkitPlayer?.ifPresent {
                    notifyPlayerOfRankUpdate(
                        it, this.activeGrant!!,
                        if (
                            subGrant != null && this.activeSubGrant?.uuid != subGrant.uuid &&
                            subGrant.getRank().uuid != RankHandler.getDefaultRank().uuid
                        )
                            subGrant else null
                    )

                    RankChangeEvent(
                        it, previousRank, activeGrant!!.rankId
                    ).dispatch()
                }
            }

            if (subGrant != null)
            {
                this.activeSubGrant = subGrant
            }

            if (activeGrant == null)
            {
                setupAutomaticGrant(grants)
            }

            if (shouldRecalculatePermissions)
            {
                handlePermissionApplication(grants, shouldCalculateNow)
            }
        }.exceptionally {
            Lemon.instance.logger.log(
                Level.WARNING, "Grant update", it
            )
            return@exceptionally null
        }
    }

    private fun checkForIpRelative()
    {
        val current = System.currentTimeMillis()

        PlayerHandler.fetchAlternateAccountsFor(uniqueId).thenAcceptAsync { lemonPlayers ->
            lemonPlayers.forEach {
                val lastIpAddress = getMetadata("last-ip-address")?.asString() ?: ""
                val targetLastIpAddress = it.getMetadata("last-ip-address")?.asString() ?: ""

                val matchingIpInfo = lastIpAddress == targetLastIpAddress

                if (matchingIpInfo)
                {
                    for (punishmentCategory in PunishmentCategory.IP_REL)
                    {
                        val punishments = PunishmentHandler
                            .fetchPunishmentsForTargetOfCategoryAndActive(it.uniqueId, punishmentCategory)
                            .join()

                        if (punishments.isNotEmpty())
                        {
                            activePunishments[IP_RELATIVE] = punishments[0]
                        }
                    }
                }
            }

            val ipRelPunishment = activePunishments[IP_RELATIVE]

            if (ipRelPunishment != null)
            {
                lazyHandleOnConnection.add {
                    CompletableFuture.supplyAsync {
                        QuickAccess.fetchColoredName(ipRelPunishment.target)
                    }.thenAccept { coloredName ->
                        val message = getIpRelMessage(
                            coloredName, ipRelPunishment
                        )

                        it.sendMessage(message)
                    }
                }
            }

            if (LemonConstants.DEBUG)
            {
                println("[Lemon] It took ${System.currentTimeMillis() - current}ms to calculate ip-relative punishments. ($name)")
            }
        }
    }

    private fun fetchPreviousRank(grants: List<Grant>): UUID?
    {
        var uuid: UUID? = null

        if (activeGrant == null)
        {
            val currentGrant = GrantRecalculationUtil.getProminentGrant(grants)

            if (currentGrant != null)
            {
                uuid = currentGrant.getRank().uuid
            }
        } else
        {
            uuid = activeGrant!!.getRank().uuid
        }

        return uuid
    }

    fun checkForGrantUpdate(): CompletableFuture<Void>
    {
        return recalculateGrants(
            shouldCalculateNow = true
        )
    }

    private fun notifyPlayerOfRankUpdate(
        player: Player, primaryGrant: Grant?, subGrant: Grant?
    )
    {
        val messenger = { grant: Grant, prefix: String, suffix: String ->
            player.sendMessage(
                "${CC.GREEN}$prefix ${grant.getRank().getColoredName()}${CC.GREEN}$suffix."
            )

            if (!grant.isPermanent)
            {
                player.sendMessage(
                    "${CC.GREEN}This rank will expire in: ${CC.WHITE}${
                        DateUtil.formatDateDiff(grant.expireDate.time)
                    }"
                )
            }
        }

        if (primaryGrant != null)
        {
            messenger(primaryGrant, "You've been granted the", " rank")
        }

        if (subGrant != null)
        {
            messenger(subGrant, "You've been granted a sub-rank of", "")
        }
    }

    private fun handlePermissionApplication(
        grants: List<Grant>, instant: Boolean = false
    )
    {
        val handleAddPermission: (String, Player) -> Unit = { it, player ->
            if (!it.startsWith("%"))
            {
                this.attachment!!
                    .setPermission(
                        it, !it.startsWith("*")
                    )

                VaultUtil.usePermissions { permission ->
                    permission.playerAdd(player, it)
                }
            }
        }

        val handlePlayerSetup: (Player) -> Unit = {
            val permissionOnlyGrants = GrantRecalculationUtil
                .getPermissionGrants(grants)

            setupPermissionAttachment(it)

            permissionOnlyGrants.forEach { grant ->
                grant.getRank().getCompoundedPermissions().forEach { permission ->
                    handleAddPermission.invoke(permission, it)
                }
            }

            this.assignedPermissions.forEach { permission ->
                handleAddPermission.invoke(permission, it)
            }

            it.recalculatePermissions()

            QuickAccess.reloadPlayer(
                this.uniqueId, recalculateGrants = false
            )
        }

        if (instant)
        {
            if (this.bukkitPlayer != null)
            {
                handlePlayerSetup.invoke(this.bukkitPlayer!!)
            }
        } else
        {
            this.handleOnConnection.add {
                handlePlayerSetup.invoke(it)
            }
        }
    }

    private fun setupPermissionAttachment(player: Player)
    {
        if (this.attachment != null)
        {
            kotlin.runCatching {
                player.removeAttachment(this.attachment!!)
            }
            player.recalculatePermissions()

            this.attachment = player.addAttachment(Lemon.instance)
        } else
        {
            this.attachment = player.addAttachment(Lemon.instance)
        }
    }

    private fun setupAutomaticGrant(grants: List<Grant>?, register: Boolean = true)
    {
        if (
            grants != null &&
            grants.any {
                it.addedReason == "Automatic (Lemon)"
            }
        )
        {
            return
        }

        val rank = RankHandler.getDefaultRank()
        activeGrant = Grant(
            UUID.randomUUID(),
            uniqueId,
            rank.uuid,
            null,
            System.currentTimeMillis(),
            Lemon.instance.settings.id,
            "Automatic (Lemon)",
            Long.MAX_VALUE
        )

        if (register)
        {
            GrantHandler.registerGrant(activeGrant!!)
        }
    }

    @JvmOverloads
    fun getColoredName(
        rank: Rank = disguiseRank() ?: realRank(bukkitPlayer),
        customColor: Boolean = true,
        ignoreMinequest: Boolean = false,
        prefixIncluded: Boolean = false
    ): String
    {
        val bukkitPlayer = bukkitPlayer

        if (
            minequest() && !ignoreMinequest
        )
        {
            return ExtHookIns.customPlayerColoredName(this, rank, prefixIncluded, customColor)
                ?: getColoredName(
                    rank, customColor, true, prefixIncluded
                )
        }

        return (if (prefixIncluded) if (ChatColor.stripColor(rank.prefix).isEmpty())
            "" else "${rank.prefix} " else "") + rank.color + (if (customColor) customColor() else "") +
                if (bukkitPlayer != null) bukkitPlayer.name else name
    }

    fun getOriginalColoredName(
        ignoreMinequest: Boolean = false,
        prefixIncluded: Boolean = false
    ): String
    {
        val bukkitPlayer = bukkitPlayer
        val rank = originalRank(bukkitPlayer)

        if (
            minequest() && !ignoreMinequest
        )
        {
            return ExtHookIns
                .customPlayerColoredNameOriginal(this, rank, prefixIncluded)
                ?: getOriginalColoredName(
                    true, prefixIncluded
                )
        }

        return rank.color + customColor() +
                if (bukkitPlayer != null) bukkitPlayer.name else name
    }

    // TODO: asdf
    fun customColor() = ""

    fun getSetting(id: String): Boolean
    {
        val data = getMetadata(id)
        return data != null && data.asBoolean()
    }

    fun declinePunishedAction(
        lambda: (String) -> Unit
    )
    {
        for (category in PunishmentCategory.WEIGHTED_DENIED)
        {
            val punishment =
                findApplicablePunishment(category)
                    ?: continue

            val extension = if (category == IP_RELATIVE)
            {
                "in relation to a ${
                    punishment.category.inf
                }."
            } else
            {
                "${punishment.category.inf}."
            }

            lambda.invoke(extension)
            return
        }
    }

    fun findApplicablePunishment(category: PunishmentCategory): Punishment?
    {
        return this.activePunishments[category]
    }

    fun hasPermission(
        permission: String,
        checkType: PermissionCheck = PermissionCheck.PLAYER
    ): Boolean
    {
        var hasPermission = false

        when (checkType)
        {
            PermissionCheck.COMPOUNDED -> hasPermission =
                activeGrant!!.getRank().getCompoundedPermissions().contains(permission)

            PermissionCheck.PLAYER -> bukkitPlayer?.ifPresent {
                if (it.isOp || it.hasPermission(permission.lowercase(Locale.getDefault())))
                {
                    hasPermission = true
                }
            }

            PermissionCheck.BOTH ->
            {
                hasPermission = activeGrant!!.getRank().getCompoundedPermissions().contains(permission)

                bukkitPlayer?.ifPresent {
                    if (it.isOp || it.hasPermission(permission.lowercase(Locale.getDefault())))
                    {
                        hasPermission = true
                    }
                }
            }
        }

        return hasPermission
    }

    fun updateOrAddMetadata(id: String, data: Metadata)
    {
        metadata[id] = data
    }

    fun removeMetadata(id: String): Metadata?
    {
        return metadata.remove(id)
    }

    fun hasMetadata(id: String): Boolean
    {
        return metadata.containsKey(id)
    }

    fun getMetadata(id: String): Metadata?
    {
        return metadata[id]
    }

    override fun save(): CompletableFuture<Void>
    {
        finalizeMetaData()

        if (config().dummyServer)
        {
            return CompletableFuture.completedFuture(null)
        }

        return DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
            .save(this, DataStoreStorageType.MONGO)
    }

    private fun finalizeMetaData()
    {
        updateOrAddMetadata(
            "last-connection", Metadata(System.currentTimeMillis())
        )

        updateOrAddMetadata(
            "last-connected-server-id", Metadata(Lemon.instance.settings.id)
        )

        updateOrAddMetadata(
            "last-connected-server-group", Metadata(Lemon.instance.settings.group)
        )

        updateOrAddMetadata(
            "last-ip-address", Metadata(ipAddress)
        )

        ipAddress?.let {
            pastIpAddresses.put(it, System.currentTimeMillis())
        }

        pastLogins[System.currentTimeMillis().toString()] = System.currentTimeMillis() - classInit

        activeGrant?.let {
            updateOrAddMetadata(
                "last-calculated-rank", Metadata(it.getRank().uuid.toString())
            )
        }
    }

    fun handlePostLoad()
    {
        recalculateGrants(
            connecting = true,
            forceRecalculatePermissions = true
        )

        recalculatePunishments(
            connecting = true
        )

        checkForIpRelative()

        handleOnConnection.add {
            checkChannelPermission(it)
        }
    }

    /**
     * Validates that the player still has the
     * permission required to access their current channel.
     */
    private fun checkChannelPermission(player: Player)
    {
        metadata["channel"]?.let { metadata ->
            val channel = ChatChannelService
                .find(metadata.asString() ?: "default")
                ?: return@let

            if (
                !channel
                    .permissionLambda
                    .invoke(player)
            )
            {
                this remove "channel"
            }
        }
    }

    fun handleIfFirstCreated()
    {
        updateOrAddMetadata(
            "first-connection",
            Metadata("${System.currentTimeMillis()}")
        )

        updateOrAddMetadata(
            "first-connection-server",
            Metadata(Lemon.instance.settings.id)
        )

        finalizeMetaData()

        save().exceptionally {
            it.printStackTrace()
            return@exceptionally null
        }

        handlePostLoad()
    }

    private fun Player?.ifPresent(block: (Player) -> Unit)
    {
        if (this != null)
        {
            block.invoke(this)
        }
    }

    infix fun has(id: String): Boolean
    {
        return metadata.containsKey(id)
    }

    infix fun doesNotHave(id: String): Boolean
    {
        return !metadata.containsKey(id)
    }

    infix fun remove(id: String)
    {
        metadata.remove(id)
    }

    fun removeMap()
    {
        bukkitPlayer?.inventory?.contents?.forEachIndexed { index, itemStack ->
            if (itemStack != null && itemStack.type == Material.MAP)
            {
                bukkitPlayer?.inventory?.setItem(index, ItemStack(Material.AIR))
            }
        }
    }

    fun performConnectionTasks()
    {
        handleOnConnection
            .forEach { it.invoke(bukkitPlayer!!) }

        Tasks.delayed(10L) {
            val player = Bukkit
                .getPlayer(this.uniqueId)
                ?: return@delayed

            lazyHandleOnConnection
                .forEach { it.invoke(player) }
        }
    }
}
