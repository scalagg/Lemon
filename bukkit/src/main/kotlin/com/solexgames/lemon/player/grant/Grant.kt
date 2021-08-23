package com.solexgames.lemon.player.grant

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.rank.Rank
import com.solexgames.lemon.util.expirable.Expireable
import java.util.*

class Grant(uuid: UUID, rankId: UUID, addedBy: UUID, addedAt: Long, addedOn: String, addedReason: String, duration: Long) : Expireable(addedAt, duration) {

    var uuid = uuid
    var rankId = rankId
    var scopes: List<String> = listOf("global")

    var addedBy = addedBy
    var addedOn = addedOn
    var addedReason = addedReason

    var removedBy: UUID? = null
    var removedAt: Long = -1
    var removedReason: String? = null
    var removed = false

    fun getRank(): Rank {
        return Lemon.instance.rankHandler.getRank(rankId).get()
    }
}