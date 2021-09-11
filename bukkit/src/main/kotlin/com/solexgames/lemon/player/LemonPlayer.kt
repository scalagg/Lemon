package com.solexgames.lemon.player

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.enums.PermissionCheck
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.metadata.Metadata
import com.solexgames.lemon.player.note.Note
import com.solexgames.lemon.util.GrantRecalculationUtil
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.type.Savable
import net.evilblock.cubed.util.CC
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

    var pastIpAddresses = mutableMapOf<String, Long>()
    var pastLogins = mutableMapOf<String, Long>()

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

    init {
        cooldowns["command"] = Cooldown(0L)
        cooldowns["request"] = Cooldown(0L)
        cooldowns["report"] = Cooldown(0L)
        cooldowns["chat"] = Cooldown(0L)
        cooldowns["slowChat"] = Cooldown(0L)
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
                if (!grant.isRemoved && grant.hasExpired()) {
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

            if (previousRank != null && activeGrant != null && previousRank != activeGrant!!.rankId) {
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
        val handleAddPermission: (String) -> Unit = {
            attachment.setPermission(it, !it.startsWith("*"))
        }
        val handlePlayerSetup: (Player) -> Unit = {
            val permissionOnlyGrants = GrantRecalculationUtil.getPermissionGrants(grants)

            setupPermissionAttachment(it)

            permissionOnlyGrants.forEach { grant ->
                grant.getRank().getCompoundedPermissions().forEach { permission ->
                    handleAddPermission.invoke(permission)
                }
            }

            it.recalculatePermissions()
        }

        if (instant) {
            getPlayer().ifPresent(handlePlayerSetup)
        } else {
            handleOnConnection.add {
                handlePlayerSetup.invoke(it)
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

    fun hasPermission(
        permission: String,
        checkType: PermissionCheck = PermissionCheck.PLAYER
    ): Boolean {
        var hasPermission = false

        when (checkType) {
            PermissionCheck.COMPOUNDED -> hasPermission = activeGrant!!.getRank().getCompoundedPermissions().contains(permission)
            PermissionCheck.PLAYER -> getPlayer().ifPresent {
                if (it.isOp || it.hasPermission(permission.toLowerCase())) {
                    hasPermission = true
                }
            }
            PermissionCheck.BOTH -> {
                hasPermission = activeGrant!!.getRank().getCompoundedPermissions().contains(permission)

                getPlayer().ifPresent {
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

    fun isStaff(): Boolean {
        return hasPermission("lemon.staff")
    }

    fun getPlayer(): Optional<Player> {
        return Optional.ofNullable(Bukkit.getPlayer(uniqueId))
    }

    override fun save(): CompletableFuture<Void> {
        finalizeMetaData()

        return Lemon.instance.mongoHandler.lemonPlayerLayer.saveEntry(uniqueId.toString(), this)
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

}
