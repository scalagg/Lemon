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
    uuid: UUID,
    name: String,
    address: String?
): Persistent<Document> {

    var notes: MutableList<Note> = mutableListOf()
    var prefixes: MutableList<String> = mutableListOf()
    var ignoring: MutableList<String> = mutableListOf()
    var permissions: MutableList<String> = mutableListOf()
    var bungeePermissions: MutableList<String> = mutableListOf()

    var uniqueId = uuid
    var username = name
    var ipAddress = address

    var commandCooldown: Cooldown = Cooldown(0L)
    var helpOpCooldown: Cooldown = Cooldown(0L)
    var reportCooldown: Cooldown = Cooldown(0L)
    var chatCooldown: Cooldown = Cooldown(0L)

    var activeGrant: Grant? = null

    private var metaData: MutableMap<String, Metadata> = mutableMapOf()

    fun updateOrAddMetadata(id: String, data: Metadata) {
        metaData[id] = data
    }

    fun removeMetadata(id: String): Metadata? {
        return metaData.remove(id)
    }

    fun getMetadata(id: String): Metadata? {
        return metaData.getOrDefault(id, null)
    }

    fun recalculateGrants() {
        Lemon.instance.grantHandler.findGrants(uniqueId).forEach { grant ->
            if (!grant.removed && !grant.hasExpired()) {
                grant.removedReason = "Expired"
                grant.removedAt = System.currentTimeMillis()
                grant.removed = true

                getPlayer().ifPresent {
                    it.sendMessage("${CC.GREEN}Your ${grant.getRank().getColoredName()} ${CC.GREEN}grant has expired.")
                }
            }
        }

        activeGrant = GrantRecalculationUtil.getProminentGrant(
            Lemon.instance.grantHandler.findGrants(uniqueId)
        )

        if (activeGrant == null) {
            val rank = Lemon.instance.rankHandler.getDefaultRank()
            val grant = Grant(UUID.randomUUID(), uniqueId, rank.uuid, null, System.currentTimeMillis(), "console", "Default", Long.MAX_VALUE)

            Lemon.instance.grantHandler.registerGrant(uniqueId, grant)
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
        TODO("Not yet implemented")
    }

}
