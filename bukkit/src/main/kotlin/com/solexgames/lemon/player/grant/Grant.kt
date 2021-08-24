package com.solexgames.lemon.player.grant

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.util.Expireable
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class Grant(uuid: UUID, rankId: UUID, addedBy: UUID, addedAt: Long, addedOn: String, addedReason: String, duration: Long) : Expireable(addedAt, duration) {

    var uuid = uuid
    var rankId = rankId
    var scopes: MutableList<String> = mutableListOf("global")

    var addedBy = addedBy
    var addedOn = addedOn
    var addedReason = addedReason

    var removedBy: UUID? = null
    var removedAt: Long = -1
    var removedReason: String? = null
    var removed = false

    fun getRank(): Rank {
        return Lemon.instance.rankHandler.getRank(rankId).orElse(Lemon.instance.rankHandler.getDefaultRank())
    }

    /**
     * Check if this grant has a scope
     * which matches the current server
     */
    fun isApplicable(): Boolean {
        if (scopes.contains("global")) {
            return true
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
}
