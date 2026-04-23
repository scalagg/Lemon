package gg.scala.lemon.player.rank

import gg.scala.lemon.scope.ServerScope

/**
 * @author GrowlyX
 * @since 2/10/2023
 */
data class RankGroup(
    val id: String,
    val permissions: List<String>,
    val scope: ServerScope? = null
)
