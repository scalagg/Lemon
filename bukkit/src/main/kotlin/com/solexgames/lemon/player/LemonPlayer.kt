package com.solexgames.lemon.player

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.enums.PermissionCheck
import com.solexgames.lemon.util.type.Persistent
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.metadata.Metadata
import com.solexgames.lemon.player.note.Note
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.GrantRecalculationUtil
import net.evilblock.cubed.util.CC
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class LemonPlayer(
    var uniqueId: UUID,
    var name: String,
    var ipAddress: String?
): Persistent<Document> {

    var notes = ArrayList<Note>()
    var ignoring = ArrayList<String>()

    var loaded = false

    var commandCooldown = Cooldown(0L)
    var helpOpCooldown = Cooldown(0L)
    var reportCooldown = Cooldown(0L)
    var chatCooldown = Cooldown(0L)
    var slowChatCooldown = Cooldown(0L)

    var activeGrant: Grant? = null

    private var metadata = HashMap<String, Metadata>()

    fun recalculateGrants() {
        val grants = Lemon.instance.grantHandler.findGrants(uniqueId)
        activeGrant = GrantRecalculationUtil.getProminentGrant(grants)

        if (activeGrant == null) {
            val rank = Lemon.instance.rankHandler.getDefaultRank()
            activeGrant = Grant(UUID.randomUUID(), uniqueId, rank.uuid, null, System.currentTimeMillis(), Lemon.instance.settings.id, "Automatic (Lemon)", Long.MAX_VALUE)

            Lemon.instance.grantHandler.registerGrant(uniqueId, activeGrant!!)
        }

        var shouldRecalculate = false

        grants.forEach { grant ->
            if (!grant.removed && !grant.hasExpired()) {
                grant.removedReason = "Expired"
                grant.removedAt = System.currentTimeMillis()
                grant.removed = true

                shouldRecalculate = true
            }
        }

        if (shouldRecalculate) {
            activeGrant = GrantRecalculationUtil.getProminentGrant(grants)

            getPlayer().ifPresent {
                it.sendMessage("${CC.GREEN}Your rank has been set to ${activeGrant!!.getRank().getColoredName()}${CC.GREEN}.")
            }
        }
    }

    fun hasPermission(
        permission: String,
        checkType: PermissionCheck = PermissionCheck.PLAYER
    ): Boolean {
        var hasPermission = false

        when (checkType) {
            PermissionCheck.COMPOUNDED -> hasPermission = activeGrant?.getRank()?.getCompoundedPermissions()?.contains(permission) ?: false
            PermissionCheck.PLAYER -> getPlayer().ifPresent {
                if (it.isOp || it.hasPermission(permission.lowercase())) {
                    hasPermission = true
                }
            }
            PermissionCheck.BOTH -> {
                hasPermission = activeGrant?.getRank()?.getCompoundedPermissions()?.contains(permission) ?: false

                getPlayer().ifPresent {
                    if (it.isOp || it.hasPermission(permission.lowercase())) {
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
        return Optional.of(Bukkit.getPlayer(uniqueId))
    }

    override fun save(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }

    override fun load(future: CompletableFuture<Document>) {
        future.whenComplete { document, throwable ->

        }
    }

}
