package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.rank.Rank
import java.util.*

object RankHandler {

    private var ranks: MutableMap<UUID, Rank> = mutableMapOf()

    fun getRank(uuid: UUID): Optional<Rank> {
        return Optional.ofNullable(ranks.getOrDefault(uuid, null))
    }

    fun getRank(name: String): Optional<Rank> {
        return ranks.values.stream()
            .filter {
                it.name.equals(name, true)
            }.findFirst()
    }

    fun getDefaultRank(): Rank {
        val optionalRank = Lemon.instance.rankHandler.getRank("Default")

        if (optionalRank.isPresent) {
            return optionalRank.get()
        }

        val rank = Rank("Default")
        rank.defaultRank = true

        return rank
    }
}
