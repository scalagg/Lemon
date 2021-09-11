package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.rank.Rank
import java.util.*

object RankHandler {

    val ranks = mutableMapOf<UUID, Rank>()

    fun loadAllRanks() {
        Lemon.instance.mongoHandler.rankLayer.fetchAllEntries().whenComplete { t, u ->
            t.forEach {
                ranks[it.value.uuid] = it.value
            }

            ranks.ifEmpty {
                createDefaultRank()
            }
        }
    }

    fun findRank(uuid: UUID): Optional<Rank> {
        return Optional.ofNullable(ranks.getOrDefault(uuid, null))
    }

    fun findRank(name: String): Rank? {
        return ranks.values.firstOrNull {
            it.name.equals(name, true)
        }
    }

    fun getDefaultRank(): Rank {
        return findRank("Default") ?: createDefaultRank()
    }

    private fun createDefaultRank(): Rank {
        val rank = Rank("Default")
        rank.defaultRank = true

        rank.save().whenComplete { _, u ->
            u?.printStackTrace()
        }

        ranks[rank.uuid] = rank

        return rank
    }
}
