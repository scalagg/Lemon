package gg.scala.lemon.player.result

import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.PaginatedResult

/**
 * @author GrowlyX
 * @since 10/9/2021
 */
object RankPaginatedResult : PaginatedResult<Rank>()
{
    override fun format(result: Rank, resultIndex: Int): String
    {
        return "${CC.PRI}#$resultIndex ${CC.WHITE}- ${result.getColoredName()} ${CC.WHITE}- ${CC.GRAY}[weight: ${result.weight}]"
    }

    override fun getHeader(page: Int, maxPages: Int): String
    {
        return "${CC.PRI}=== ${CC.SEC}Ranks ${CC.GRAY}($page/$maxPages) ${CC.PRI}==="
    }
}
