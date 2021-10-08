package gg.scala.lemon.player.comment.output

import gg.scala.lemon.player.comment.Comment
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.PaginatedResult
import net.evilblock.cubed.util.time.TimeUtil

/**
 * @author GrowlyX
 * @since 10/8/2021
 */
object CommentPaginatedResult : PaginatedResult<Comment>()
{
    override fun format(result: Comment, resultIndex: Int): String
    {
        return "${CC.GRAY} - ${CC.SEC}${CubedCacheUtil.fetchName(result.issuer)} ${CC.GRAY}(${TimeUtil.formatIntoFullCalendarString(result.timestamp)})${CC.WHITE}: ${result.value}"
    }

    override fun getHeader(page: Int, maxPages: Int): String
    {
        return "${CC.SEC}Comments ${CC.GRAY}($page/$maxPages)${CC.SEC}:"
    }
}
