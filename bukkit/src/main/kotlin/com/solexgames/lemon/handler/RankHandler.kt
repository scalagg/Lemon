package com.solexgames.lemon.handler

import com.solexgames.lemon.player.rank.Rank
import java.util.*

object RankHandler {

    var ranks: MutableMap<UUID, Rank> = mutableMapOf()

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

        val rank = Rank("Default")
        rank.defaultRank = true

        return rank
    }
}
