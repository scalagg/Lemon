package gg.scala.lemon.player

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.*
import gg.scala.lemon.player.enums.PermissionCheck
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.player.note.Note
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategory.*
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.util.GrantRecalculationUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.SplitUtil
import gg.scala.lemon.util.VaultUtil
import gg.scala.lemon.util.other.Cooldown
import gg.scala.lemon.util.type.Savable
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.permissions.PermissionAttachment
import java.util.*
import java.util.concurrent.CompletableFuture

class LemonPlayer(
    var uniqueId: UUID,
    var name: String,

    @Transient
    var ipAddress: String?
) : Savable {

    private val bungeePermissions = mutableListOf<String>()

    var previousIpAddress: String? = null

    var pastIpAddresses = mutableMapOf<String, Long>()
    var pastLogins = mutableMapOf<String, Long>()

    val activePunishments = mutableMapOf<PunishmentCategory, Punishment?>()

    var notes = mutableListOf<Note>()
    var ignoring = mutableListOf<UUID>()

    @Transient
    val cooldowns = mutableMapOf<String, Cooldown>()

    @Transient
    val handleOnConnection = arrayListOf<(Player) -> Any>()

    @Transient
    var activeGrant: Grant? = null

    @Transient
    lateinit var attachment: PermissionAttachment

    var metadata = mutableMapOf<String, Metadata>()

    val isStaff: Boolean
        get() = hasPermission("lemon.staff")

    val bukkitPlayer: Player?
        get() = Bukkit.getPlayer(uniqueId)

    val classInit = System.currentTimeMillis()

    init {
        cooldowns["command"] = Cooldown(0L)
        cooldowns["request"] = Cooldown(0L)
        cooldowns["report"] = Cooldown(0L)
        cooldowns["chat"] = Cooldown(0L)
        cooldowns["slowChat"] = Cooldown(0L)

        for (value in PunishmentCategory.VALUES) {
            activePunishments[value] = null
        }
    }

    fun recalculatePunishments(
        connecting: Boolean = false,
        nothing: Boolean = false
    ): CompletableFuture<Void> {
        return PunishmentHandler
            .fetchAllPunishmentsForTarget(uniqueId).thenAccept { list ->
                list.forEach { QuickAccess.attemptExpiration(it) }

                for (value in PunishmentCategory.VALUES) {
                    val newList = list.filter {
                        it.category == value && it.isActive
                    }

                    if (newList.isEmpty()) {
                        activePunishments[value] = null
                        continue
                    }

                    activePunishments[value] = newList[0]
                }

                if (nothing) return@thenAccept

                if (!connecting) {
                    activePunishments.forEach {
                        if (it.value != null) {
                            val message = getPunishmentMessage(it.value!!)

                            when (it.value!!.category.intensity) {
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
                } else {
                    val sortedCategories = PunishmentCategory.PERSISTENT.sortedByDescending { it.ordinal }

                    for (sortedCategory in sortedCategories) {
                        val punishmentInCategory = activePunishments[sortedCategory]

                        if (punishmentInCategory != null) {
                            val message = getPunishmentMessage(punishmentInCategory, current = false)

                            handleOnConnection.add {
                                it.sendMessage(message)
                            }

                            return@thenAccept
                        }
                    }
                }
            }
    }

    fun getPunishmentMessage(punishment: Punishment, current: Boolean = true): String {
        return when (punishment.category) {
            KICK -> """
                ${CC.RED}You were kicked from ${Lemon.instance.settings.id}:
                ${CC.WHITE}${punishment.addedReason}
            """.trimIndent()
            MUTE -> """
                ${CC.RED}${if (current) "You've been" else "You're currently"} muted for: ${CC.WHITE}${punishment.addedReason}
                ${CC.RED}This punishment will ${punishment.fancyDurationFromNowStringRaw}.
            """.trimIndent()
            BAN -> if (punishment.isPermanent) {
                String.format(
                    Lemon.instance.languageConfig.permBanMessage,
                    punishment.addedReason,
                    SplitUtil.splitUuid(punishment.uuid)
                )
            } else {
                String.format(
                    Lemon.instance.languageConfig.tempBanMessage,
                    punishment.durationString,
                    punishment.addedReason,
                    SplitUtil.splitUuid(punishment.uuid)
                )
            }
            BLACKLIST -> Lemon.instance.languageConfig.blacklistMessage
        }
    }

    fun recalculateGrants(
        autoNotify: Boolean = false,
        forceRecalculatePermissions: Boolean = false,
        shouldCalculateNow: Boolean = false,
        connecting: Boolean = false
    ): CompletableFuture<Void> {
        return GrantHandler.fetchGrantsFor(uniqueId).thenAccept { grants ->
            if (grants == null || grants.isEmpty()) {
                setupAutomaticGrant()

                return@thenAccept
            }

            var shouldNotifyPlayer = autoNotify
            val previousRank = fetchPreviousRank(grants)

            grants.forEach { grant ->
                if (!grant.isRemoved && grant.hasExpired) {
                    grant.removedReason = "Expired"
                    grant.removedAt = System.currentTimeMillis()
                    grant.removedOn = Lemon.instance.settings.id
                    grant.isRemoved = true

                    grant.save()

                    shouldNotifyPlayer = true
                }
            }

            activeGrant = GrantRecalculationUtil.getProminentGrant(grants)

            var shouldRecalculatePermissions = forceRecalculatePermissions

            if (previousRank != null && activeGrant != null && previousRank != activeGrant!!.getRank().uuid) {
                shouldRecalculatePermissions = true
                shouldNotifyPlayer = true
            }

            if (shouldNotifyPlayer && !connecting) {
                bukkitPlayer?.ifPresent {
                    notifyPlayerOfRankUpdate(it)
                }
            }

            if (activeGrant == null) {
                setupAutomaticGrant()
            }

            if (shouldRecalculatePermissions) handlePermissionApplication(grants, shouldCalculateNow)
        }
    }

    fun pushCocoaUpdates() {
        RedisHandler.buildMessage(
            "permission-update",
            hashMapOf<String, String>().also {
                it["uniqueId"] = uniqueId.toString()
                it["currentDisplayName"] = getColoredName()
                it["currentBungeePermissions"] = bungeePermissions.joinToString(
                    separator = ","
                )
            }
        ).dispatch("cocoa")
    }

    private fun fetchPreviousRank(grants: List<Grant>): UUID? {
        var uuid: UUID? = null

        if (activeGrant == null) {
            val currentGrant = GrantRecalculationUtil.getProminentGrant(grants)

            if (currentGrant != null) {
                uuid = currentGrant.getRank().uuid
            }
        } else {
            uuid = activeGrant!!.getRank().uuid
        }

        return uuid
    }

    fun checkForGrantUpdate() {
        recalculateGrants(
            shouldCalculateNow = true
        )
    }

    private fun notifyPlayerOfRankUpdate(player: Player) {
        activeGrant?.let { grant ->
            player.sendMessage("${CC.GREEN}Your rank has been set to ${grant.getRank().getColoredName()}${CC.GREEN}.")
        }
    }

    private fun handlePermissionApplication(grants: List<Grant>, instant: Boolean = false) {
        val handleAddPermission: (String, Player) -> Unit = { it, player ->
            if (it.startsWith("%")) {
                bungeePermissions.add(it.removePrefix("%"))
            } else {
                attachment.setPermission(it, !it.startsWith("*"))

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

            it.recalculatePermissions()

            QuickAccess.reloadPlayer(
                uniqueId,
                recalculateGrants = false
            )
        }

        if (instant) {
            if (bukkitPlayer != null) {
                handlePlayerSetup.invoke(bukkitPlayer!!)
                pushCocoaUpdates()
            }
        } else {
            handleOnConnection.add {
                handlePlayerSetup.invoke(it)
                pushCocoaUpdates()
            }
        }
    }

    private fun setupPermissionAttachment(player: Player) {
        this.attachment = player.addAttachment(Lemon.instance)
    }

    private fun setupAutomaticGrant() {
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

    fun getColoredName(): String {
        return activeGrant!!.getRank().color + name
    }

    fun getSetting(id: String): Boolean {
        val data = getMetadata(id)
        return data != null && data.asBoolean()
    }

    fun fetchPunishmentOf(category: PunishmentCategory): Punishment? {
        return this.activePunishments[category]
    }

    fun hasPermission(
        permission: String,
        checkType: PermissionCheck = PermissionCheck.PLAYER
    ): Boolean {
        var hasPermission = false

        when (checkType) {
            PermissionCheck.COMPOUNDED -> hasPermission =
                activeGrant!!.getRank().getCompoundedPermissions().contains(permission)
            PermissionCheck.PLAYER -> bukkitPlayer?.ifPresent {
                if (it.isOp || it.hasPermission(permission.toLowerCase())) {
                    hasPermission = true
                }
            }
            PermissionCheck.BOTH -> {
                hasPermission = activeGrant!!.getRank().getCompoundedPermissions().contains(permission)

                bukkitPlayer?.ifPresent {
                    if (it.isOp || it.hasPermission(permission.toLowerCase())) {
                        hasPermission = true
                    }
                }
            }
        }

        return hasPermission
    }

    fun resetChatCooldown() {
        val donor = hasPermission("lemon.donator")

        cooldowns["chat"] = if (donor) {
            Cooldown(1000L)
        } else {
            Cooldown(3000L)
        }
    }

    fun updateOrAddMetadata(id: String, data: Metadata) {
        metadata[id] = data
    }

    fun removeMetadata(id: String): Metadata? {
        return metadata.remove(id)
    }

    fun hasMetadata(id: String): Boolean {
        return metadata.containsKey(id)
    }

    fun getMetadata(id: String): Metadata? {
        return metadata.getOrDefault(id, null)
    }

    override fun save(): CompletableFuture<Void> {
        finalizeMetaData()

        return DataStoreHandler.lemonPlayerLayer.saveEntry(
            uniqueId.toString(), this
        )
    }

    private fun finalizeMetaData() {
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

    fun handlePostLoad() {
        recalculateGrants(
            connecting = true,
            forceRecalculatePermissions = true
        )

        recalculatePunishments(
            connecting = true
        )
    }

    fun handleIfFirstCreated() {
        updateOrAddMetadata(
            "first-connection", Metadata(System.currentTimeMillis())
        )

        finalizeMetaData()

        save().whenComplete { _, u ->
            u?.printStackTrace()
        }

        handlePostLoad()
    }

    private fun Player.ifPresent(block: (Player) -> Unit) {
        block.invoke(this)
    }

}
