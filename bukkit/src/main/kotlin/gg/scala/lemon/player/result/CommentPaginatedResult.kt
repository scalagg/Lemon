package gg.scala.lemon.player.result

import gg.scala.lemon.player.comment.Comment
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.PaginatedResult

/**
 * @author GrowlyX
 * @since 10/8/2021
 */
object CommentPaginatedResult : PaginatedResult<Comment>()
{
    override fun format(result: Comment, resultIndex: Int): String
    {
        return "${CC.PRI}#$resultIndex ${CC.WHITE}- ${CC.SEC}${CubedCacheUtil.fetchName(result.issuer)} ${CC.GRAY}(${result.shortenedUniqueId})${CC.WHITE}: ${result.value}"
    }

    override fun getHeader(page: Int, maxPages: Int): String
    {
        return "${CC.PRI}=== ${CC.SEC}Notes ${CC.GRAY}($page/$maxPages) ${CC.PRI}==="
    }
}
