package com.solexgames.lemon.player

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.model.Persistent
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.player.metadata.Metadata
import com.solexgames.lemon.player.note.Note
import com.solexgames.lemon.util.Cooldown
import com.solexgames.lemon.util.GrantRecalculationUtil
import net.evilblock.cubed.util.CC
import org.bson.Document
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

class LemonPlayer(
    var uniqueId: UUID,
    var name: String,
    var ipAddress: String?
): Persistent<Document> {

    var notes: MutableList<Note> = mutableListOf()
    var ignoring: MutableList<String> = mutableListOf()

    var loaded: Boolean = false

    var commandCooldown: Cooldown = Cooldown(0L)
    var helpOpCooldown: Cooldown = Cooldown(0L)
    var reportCooldown: Cooldown = Cooldown(0L)
    var chatCooldown: Cooldown = Cooldown(0L)

    var activeGrant: Grant? = null

    private var metaData: MutableMap<String, Metadata> = mutableMapOf()

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

    fun hasPermission(permission: String, ignorePlayer: Boolean = false): Boolean {
        var boolean = activeGrant?.getRank()?.getCompoundedPermissions()?.contains(permission) ?: false

        if (!ignorePlayer) {
            getPlayer().ifPresent {
                if (it.isOp || it.hasPermission(permission.lowercase())) boolean = true
            }
        }

        return boolean
    }

    fun updateOrAddMetadata(id: String, data: Metadata) {
        metaData[id] = data
    }

    fun removeMetadata(id: String): Metadata? {
        return metaData.remove(id)
    }

    fun hasMetadata(id: String): Boolean {
        return metaData.containsKey(id)
    }

    fun getMetadata(id: String): Metadata? {
        return metaData.getOrDefault(id, null)
    }

    fun isStaff(): Boolean {
        return hasPermission("lemon.staff", true)
    }

    fun getPlayer(): Optional<Player> {
        return Optional.of(Bukkit.getPlayer(uniqueId))
    }

    override fun save(): CompletableFuture<Void> {
        TODO("Not yet implemented")
    }

    override fun load(future: CompletableFuture<Document>) {
        future.whenComplete { t, u ->

        }
    }

}
