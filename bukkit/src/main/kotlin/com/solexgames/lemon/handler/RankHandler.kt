package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.rank.Rank
import java.util.*

object RankHandler {

    val ranks = mutableMapOf<UUID, Rank>()

    fun loadAllRanks() {
        Lemon.instance.mongoHandler.rankCollection.find().forEach {
            val rank = LemonConstants.GSON.fromJson(
                it.toJson(), Rank::class.java
            )

            if (rank != null) ranks[rank.uuid] = rank
        }

        ranks.ifEmpty {
            createDefaultRank()
        }
    }

    fun findRank(uuid: UUID): Optional<Rank> {
        return Optional.ofNullable(ranks.getOrDefault(uuid, null))
    }

    fun findRank(name: String): Optional<Rank> {
        return ranks.values.stream()
            .filter {
                it.name.equals(name, true)
            }.findFirst()
    }

    fun getDefaultRank(): Rank {
        val optionalRank = findRank("Default")

        if (optionalRank.isPresent) {
            return optionalRank.get()
        }

        return createDefaultRank()
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
