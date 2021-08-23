package com.solexgames.lemon.handler

import com.solexgames.lemon.player.rank.Rank
import java.util.*

class RankHandler {

    var ranks: Map<UUID, Rank> = mapOf()

    fun getRank(uuid: UUID): Optional<Rank> {
        return Optional.ofNullable(ranks.getOrDefault(uuid, null))
    }

    fun getRank(name: String): Optional<Rank> {
        return ranks.values.stream().filter { it.name.equals(name, true) }.findFirst()
    }
}