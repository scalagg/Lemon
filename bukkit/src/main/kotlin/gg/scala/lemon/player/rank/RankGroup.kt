package gg.scala.lemon.player.rank

import gg.scala.lemon.scope.ServerScope

data class RankGroup(
    val id: String,
    val permissions: List<String>,
    val scope: ServerScope? = null
)
