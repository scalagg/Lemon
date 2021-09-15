package com.solexgames.lemon.player

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.lemon.player.enums.PermissionCheck
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.metadata.Metadata
import com.solexgames.lemon.player.note.Note
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategory.*
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import com.solexgames.lemon.util.GrantRecalculationUtil
import com.solexgames.lemon.util.QuickAccess
import com.solexgames.lemon.util.SplitUtil
import com.solexgames.lemon.util.VaultUtil
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.type.Savable
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
): Savable {

    private val bungeePermissions = mutableListOf<String>()

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
    ) {
        val punishments = Lemon.instance.punishmentHandler
            .fetchAllPunishmentsForTarget(uniqueId)

        punishments.thenAccept { list ->
            val currentMap = activePunishments.toMutableMap()

            list.forEach { QuickAccess.attemptExpiration(it) }

            for (value in PunishmentCategory.VALUES) {
                val newList = list.filter {
                    it.category == value && (it.category.instant || it.isActive)
                }

                if (newList.isEmpty()) {
                    continue
                }

                activePunishments[value] = newList[0]
            }

            if (!connecting) {
                currentMap.forEach {
                    val activeValue = activePunishments[it.key]

                    if (it.value == null && activeValue != null) {
                        val message = getPunishmentMessage(activeValue)

                        when (activeValue.category.intensity) {
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
                        val message = getPunishmentMessage(punishmentInCategory)

                        handleOnConnection.add {
                            it.sendMessage(message)
                        }

                        return@thenAccept
                    }
                }
            }
        }
    }

    fun getPunishmentMessage(punishment: Punishment): String {
        return when (punishment.category) {
            KICK -> """
                ${CC.RED}You were kicked from ${Lemon.instance.settings.id}:
                ${CC.WHITE}${punishment.addedReason}
            """.trimIndent()
            MUTE -> """
                ${CC.RED}You're muted for: ${CC.WHITE}${punishment.addedReason}
                ${CC.RED}This punishment will ${punishment.fancyDurationStringRaw}.
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
    ) {
        val completableFuture = Lemon.instance.grantHandler.fetchGrantsFor(uniqueId)

        completableFuture.whenComplete { grants, _ ->
            if (grants == null || grants.isEmpty()) {
                setupAutomaticGrant()

                return@whenComplete
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
                handleOnConnection.add {
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
        ).publishAsync("cocoa")
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
        val rank = Lemon.instance.rankHandler.getDefaultRank()
        activeGrant = Grant(UUID.randomUUID(), uniqueId, rank.uuid, null, System.currentTimeMillis(), Lemon.instance.settings.id, "Automatic (Lemon)", Long.MAX_VALUE)

        Lemon.instance.grantHandler.registerGrant(activeGrant!!)
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
            PermissionCheck.COMPOUNDED -> hasPermission = activeGrant!!.getRank().getCompoundedPermissions().contains(permission)
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

        return Lemon.instance.mongoHandler.lemonPlayerLayer.saveEntry(
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
