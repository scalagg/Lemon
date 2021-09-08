package com.solexgames.lemon.player

import com.google.gson.annotations.Expose
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
import java.util.*
import java.util.concurrent.CompletableFuture

class LemonPlayer(
    var uniqueId: UUID,
    var name: String,
    var ipAddress: String?
): Savable {

    var pastIpAddresses = mutableMapOf<String, Long>()
    var pastLogins = mutableMapOf<String, Long>()

    var notes = mutableListOf<Note>()
    var ignoring = mutableListOf<UUID>()

    @Expose(deserialize = false, serialize = false)
    var commandCooldown = Cooldown(0L)

    @Expose(deserialize = false, serialize = false)
    var requestCooldown = Cooldown(0L)

    @Expose(deserialize = false, serialize = false)
    var reportCooldown = Cooldown(0L)

    @Expose(deserialize = false, serialize = false)
    var chatCooldown = Cooldown(0L)

    @Expose(deserialize = false, serialize = false)
    var slowChatCooldown = Cooldown(0L)

    @Expose(deserialize = false, serialize = false)
    lateinit var activeGrant: Grant

    private var metadata = HashMap<String, Metadata>()

    fun recalculateGrants() {
        val completableFuture = Lemon.instance.grantHandler.fetchGrantsFor(uniqueId)
        var shouldRecalculate = false

        completableFuture.whenComplete { it, throwable ->
            throwable?.printStackTrace()

            if (it == null || it.isEmpty()) {
                setupAutomaticGrant()

                return@whenComplete
            }

            it.forEach { grant ->
                if (!grant.removed && !grant.hasExpired()) {
                    grant.removedReason = "Expired"
                    grant.removedAt = System.currentTimeMillis()
                    grant.removed = true

                    shouldRecalculate = true
                }
            }

            if (shouldRecalculate) {
                activeGrant = GrantRecalculationUtil.getProminentGrant(it)

                getPlayer().ifPresent { player ->
                    player.sendMessage("${CC.GREEN}Your rank has been set to ${activeGrant.getRank().getColoredName()}${CC.GREEN}.")
                }
            }
        }
    }

    fun setupAutomaticGrant() {
        val rank = Lemon.instance.rankHandler.getDefaultRank()
        activeGrant = Grant(UUID.randomUUID(), uniqueId, rank.uuid, null, System.currentTimeMillis(), Lemon.instance.settings.id, "Automatic (Lemon)", Long.MAX_VALUE)

        Lemon.instance.grantHandler.registerGrant(activeGrant)
    }

    fun getColoredName(): String {
        return activeGrant.getRank().color + name
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
            PermissionCheck.COMPOUNDED -> hasPermission = activeGrant.getRank().getCompoundedPermissions().contains(permission)
            PermissionCheck.PLAYER -> getPlayer().ifPresent {
                if (it.isOp || it.hasPermission(permission.toLowerCase())) {
                    hasPermission = true
                }
            }
            PermissionCheck.BOTH -> {
                hasPermission = activeGrant.getRank().getCompoundedPermissions().contains(permission)

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

        chatCooldown = if (donor) {
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

        updateOrAddMetadata(
            "last-calculated-rank", Metadata(activeGrant.getRank().uuid.toString())
        )
    }

    fun handlePostLoad() {
        recalculateGrants()
    }

    fun handleIfFirstCreated() {
        updateOrAddMetadata(
            "first-connection", Metadata(System.currentTimeMillis())
        )

        finalizeMetaData()

        save().whenComplete { _, u ->
            u?.printStackTrace()
        }
    }

}
