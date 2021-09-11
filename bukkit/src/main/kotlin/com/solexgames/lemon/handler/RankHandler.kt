package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import java.util.*

object RankHandler {

    val ranks = mutableMapOf<UUID, Rank>()

    fun loadAllRanks() {
        Lemon.instance.mongoHandler.rankLayer.fetchAllEntries().whenComplete { t, _ ->
            t.forEach {
                ranks[it.value.uuid] = it.value
            }

            ranks.ifEmpty {
                createDefaultRank()
            }
        }
    }

    fun findRank(uuid: UUID): Rank? {
        return ranks.values.firstOrNull {
            it.uuid == uuid
        }
    }

    fun findRank(name: String): Rank? {
        return ranks.values.firstOrNull {
            it.name.equals(name, true)
        }
    }

    fun getDefaultRank(): Rank {
        return findRank("Default") ?: createDefaultRank()
    }

    fun getSortedRankString(): String {
        return ranks.values.sortedBy { -it.weight }
            .map { it.getColoredName() }
            .joinToString(separator = "${CC.WHITE}, ")
    }

    fun getSorted(): List<Rank> {
        return ranks.values.sortedByDescending { it.weight }
    }

    private fun createDefaultRank(): Rank {
        val rank = Rank("Default")
        rank.saveAndPushUpdatesGlobally()

        ranks[rank.uuid] = rank

        return rank
    }
}
