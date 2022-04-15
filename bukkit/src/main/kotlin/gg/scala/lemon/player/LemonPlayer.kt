package gg.scala.lemon.player

import com.google.zxing.WriterException
import gg.scala.common.Savable
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.LemonConstants.AUTH_PREFIX
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.*
import gg.scala.lemon.player.color.PlayerColorHandler
import gg.scala.lemon.player.enums.PermissionCheck
import gg.scala.lemon.player.event.impl.RankChangeEvent
import gg.scala.lemon.player.extension.PlayerCachingExtension
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategory.*
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.ClientUtil.handleApplicableClient
import gg.scala.lemon.util.GrantRecalculationUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.originalRank
import gg.scala.lemon.util.QuickAccess.realRank
import gg.scala.lemon.util.SplitUtil
import gg.scala.lemon.util.VaultUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Schedulers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.totp.ImageMapRenderer
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.permissions.PermissionAttachment
import java.util.*
import java.util.concurrent.CompletableFuture

class LemonPlayer(
    var uniqueId: UUID,
    var name: String,

    @JvmField
    @Transient
    var ipAddress: String?
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    var previousIpAddress: String? = null

    var pastIpAddresses = mutableMapOf<String, Long>()
    var pastLogins = mutableMapOf<String, Long>()

    val activePunishments = mutableMapOf<PunishmentCategory, Punishment?>()
    var permissions = listOf<String>()

    var ignoring = mutableListOf<UUID>()

    val handleOnConnection = arrayListOf<(Player) -> Any>()
    val lateHandleOnConnection = arrayListOf<(Player) -> Any>()

    var activeGrant: Grant? = null

    private var attachment: PermissionAttachment? = null

    var metadata = mutableMapOf<String, Metadata>()

    val bukkitPlayer: Player?
        get() = Bukkit.getPlayer(uniqueId)

    var savePreviousIpAddressAsCurrent = false

    private val classInit = System.currentTimeMillis()

    init
    {
        for (value in PunishmentCategory.VALUES)
        {
            activePunishments[value] = null
        }
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
                                PunishmentCategoryIntensity.LIGHT -> bukkitPlayer?.ifPresent { player ->
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

                            lateHandleOnConnection
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
            KICK -> """
                ${CC.RED}You've been kicked from ${Lemon.instance.settings.id}:
                ${CC.WHITE}${punishment.addedReason}
            """.trimIndent()
            MUTE -> """
                ${CC.RED}${if (current) "You've been" else "You're currently"} muted for: ${CC.WHITE}${punishment.addedReason}
                ${CC.RED}This punishment will ${punishment.fancyDurationFromNowStringRaw}.
            """.trimIndent()
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
        val current = System.currentTimeMillis()

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
                    grant.isRemoved = true

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

            if (previousRank != null && activeGrant != null && previousRank != activeGrant!!.getRank().uuid)
            {
                shouldRecalculatePermissions = true
                shouldNotifyPlayer = true
            }

            if (shouldNotifyPlayer && !connecting)
            {
                bukkitPlayer?.ifPresent {
                    notifyPlayerOfRankUpdate(it)

                    RankChangeEvent(
                        it, previousRank, activeGrant!!.rankId
                    ).dispatch()
                }
            }

            if (activeGrant == null)
            {
                setupAutomaticGrant(grants)
            }

            if (shouldRecalculatePermissions)
                handlePermissionApplication(grants, shouldCalculateNow)

            if (connecting && LemonConstants.DEBUG)
            {
                println("[Lemon] It took ${System.currentTimeMillis() - current}ms to calculate grants. ($name)")
            }
        }
    }

    fun isAuthExempt(): Boolean
    {
        return getSetting("auth-exempt")
    }

    fun hasAuthenticatedThisSession(): Boolean
    {
        return bukkitPlayer?.hasMetadata("authenticated") == true
    }

    fun hasSetupAuthentication(): Boolean
    {
        return getMetadata("auth-secret") != null
    }

    fun getAuthSecret(): String
    {
        return getMetadata("auth-secret")?.asString() ?: ""
    }

    private fun validatePlayerAuthentication()
    {
        if (!hasPermission("lemon.2fa.forced"))
            return

        if (isAuthExempt())
        {
            authenticateInternal()
            return
        }

        val authSecret = getMetadata("auth-secret")

        if (authSecret != null)
        {
            if (this.previousIpAddress != null && this.previousIpAddress == ipAddress)
            {
                authenticateInternal()
            } else
            {
                savePreviousIpAddressAsCurrent = true; save()

                Schedulers.sync().callLater({
                    bukkitPlayer?.sendMessage("${AUTH_PREFIX}${CC.SEC}Please authenticate using ${CC.WHITE}/auth <code>${CC.SEC}.")
                }, 1L)
            }
        } else
        {
            Schedulers.sync().callLater({
                bukkitPlayer?.sendMessage("${AUTH_PREFIX}${CC.SEC}Please setup authentication using ${CC.WHITE}/setup2fa${CC.SEC}.")
            }, 1L)
        }
    }

    fun authenticateInternal()
    {
        bukkitPlayer?.setMetadata(
            "authenticated",
            FixedMetadataValue(Lemon.instance, true)
        )

        updateOrAddMetadata(
            "last-auth",
            Metadata(System.currentTimeMillis().toString())
        )
    }

    fun authenticateInternalReversed()
    {
        bukkitPlayer?.removeMetadata(
            "authenticated",
            Lemon.instance
        )

        this remove "last-auth"
    }

    fun handleAuthMap(authSecret: String)
    {
        val mapRenderer: ImageMapRenderer

        try
        {
            mapRenderer = ImageMapRenderer(
                name, authSecret, LemonConstants.WEB_LINK
            )
        } catch (e: WriterException)
        {
            println("[Lemon] [2FA] An error occurred: ${e.message}")

            bukkitPlayer?.sendMessage(
                arrayOf(
                    "${CC.RED}While setting your 2FA, an error occurred.",
                    "${CC.RED}This error has been reported, sorry for the inconvenience."
                )
            )
            return
        }

        val notNullPlayer = bukkitPlayer!!

        val stack = ItemStack(Material.MAP)
        val view = Bukkit.createMap(notNullPlayer.world)

        stack.durability = view.id
        stack.amount = 0

        notNullPlayer.inventory.heldItemSlot = 4
        notNullPlayer.itemInHand = stack

        val down = notNullPlayer.location
        down.pitch = 90F

        notNullPlayer.teleport(down)

        view.renderers.forEach {
            view.removeRenderer(it)
        }

        view.addRenderer(mapRenderer)

        notNullPlayer.sendMap(view)
        notNullPlayer.updateInventory()
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
                lateHandleOnConnection.add {
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

    private fun notifyPlayerOfRankUpdate(player: Player)
    {
        activeGrant?.let { grant ->
            player.sendMessage("${CC.GREEN}Your rank has been set to ${grant.getRank().getColoredName()}${CC.GREEN}.")
        }
    }

    private fun handlePermissionApplication(grants: List<Grant>, instant: Boolean = false)
    {
        val handleAddPermission: (String, Player) -> Unit = { it, player ->
            if (!it.startsWith("%"))
            {
                attachment!!.setPermission(it, !it.startsWith("*"))

                VaultUtil.usePermissions { permission ->
                    permission.playerAdd(player, it)
                }
            }
        }
        val handlePlayerSetup: (Player) -> Unit = {
            val permissionOnlyGrants = GrantRecalculationUtil.getPermissionGrants(grants)

            setupPermissionAttachment(it)

            permissionOnlyGrants.forEach { grant ->
                grant.getRank().getCompoundedPermissions().forEach { permission ->
                    handleAddPermission.invoke(permission, it)
                }
            }

            permissions.forEach { permission ->
                handleAddPermission.invoke(permission, it)
            }

            it.recalculatePermissions()

            QuickAccess.reloadPlayer(
                uniqueId, recalculateGrants = false
            )
        }

        if (instant)
        {
            if (bukkitPlayer != null)
            {
                handlePlayerSetup.invoke(bukkitPlayer!!)
            }
        } else
        {
            handleOnConnection.add {
                handlePlayerSetup.invoke(it)
            }
        }
    }

    private fun setupPermissionAttachment(player: Player)
    {
        if (this.attachment != null)
        {
            this.attachment!!.permissions.clear()
        } else
        {
            this.attachment = player.addAttachment(Lemon.instance)
        }
    }

    private fun setupAutomaticGrant(grants: List<Grant>?)
    {
        if (
            grants != null && grants.firstOrNull { it.addedReason == "Automatic (Lemon)" } != null
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

        GrantHandler.registerGrant(activeGrant!!)
    }

    @JvmOverloads
    fun getColoredName(
        rank: Rank = realRank(bukkitPlayer),
        customColor: Boolean = true
    ): String
    {
        val bukkitPlayer = bukkitPlayer

        return rank.color + (if (customColor) customColor() else "") +
                if (bukkitPlayer != null) bukkitPlayer.name else name
    }

    fun getOriginalColoredName(): String
    {
        val bukkitPlayer = bukkitPlayer
        val rank = originalRank(bukkitPlayer)

        return rank.color + customColor() +
                if (bukkitPlayer != null) bukkitPlayer.name else name
    }

    fun customColor(): String
    {
        val metadata = getMetadata("color")

        return if (metadata != null)
        {
            val color = PlayerColorHandler.find(metadata.asString())

            color?.chatColor?.toString() ?: ""
        } else ""
    }

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
                }"
            } else
            {
                punishment.category.inf
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
        ).thenRun {
            PlayerCachingExtension
                .memorize(this).join()
        }

        recalculatePunishments(
            connecting = true
        )

        checkForIpRelative()

        handleOnConnection.add {
            validatePlayerAuthentication()
            checkChannelPermission(it)

            Tasks.delayed(1L) {
                handleAutomaticStaffModules(it)
            }
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
                .find(metadata.asString())
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

    private fun handleAutomaticStaffModules(player: Player)
    {
        if (
            player.hasPermission("lemon.staff")
        )
        {
            handleApplicableClient(player) {
                it.enableStaffModules(player)

                player.sendMessage("${CC.GREEN}${it.getClientName()} staff modules have been enabled.")
            }
        }
    }

    fun handleIfFirstCreated()
    {
        updateOrAddMetadata(
            "first-connection",
            Metadata(System.currentTimeMillis())
        )

        finalizeMetaData()

        save().whenComplete { _, u ->
            u?.printStackTrace()
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
}
