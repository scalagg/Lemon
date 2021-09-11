package com.solexgames.lemon.player.grant

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.util.other.Expirable
import com.solexgames.lemon.util.type.Savable
import net.evilblock.cubed.util.bukkit.Tasks
import java.util.*
import java.util.concurrent.CompletableFuture

class Grant(
    val uuid: UUID,
    var target: UUID,
    var rankId: UUID,
    var addedBy: UUID?,
    addedAt: Long,
    var addedOn: String,
    var addedReason: String,
    duration: Long
): Expirable(addedAt, duration), Savable {

    var scopes: MutableList<String> = mutableListOf("global")

    var removedReason: String? = null
    var removedOn: String? = null
    var removedBy: UUID? = null
    var removedAt: Long = -1
    var removed = false

    fun getRank(): Rank {
        return Lemon.instance.rankHandler.findRank(rankId) ?: Lemon.instance.rankHandler.getDefaultRank()
    }

    /**
     * Check if this grant has a scope
     * which matches the current server
     */
    fun isApplicable(): Boolean {
        if (scopes.contains("global")) {
            return scopes.contains("global")
        }

        var boolean = false

        scopes.forEach {
            if (Lemon.instance.settings.id.equals(it, true)) {
                boolean = true
                return@forEach
            }
        }

        return boolean
    }

    fun canRemove(lemonPlayer: LemonPlayer): Boolean {
        return lemonPlayer.activeGrant!!.getRank().weight >= getRank().weight && !removed && !isAutoGrant()
    }

    fun isAutoGrant(): Boolean {
        return addedReason == "Automatic (Lemon)" && addedBy == null
    }

    override fun save(): CompletableFuture<Void> {
        Tasks.asyncDelayed(2L) {
            RedisHandler.buildMessage(
                "recalculate-grants",
                mutableMapOf<String, String>().also {
                    it["target"] = target.toString()
                }
            )
        }

        return Lemon.instance.mongoHandler.grantLayer.saveEntry(uuid.toString(), this)
    }
}
