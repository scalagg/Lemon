package gg.scala.lemon.handler

import com.mongodb.client.model.Filters
import gg.scala.lemon.player.comment.Comment
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.SplitUtil
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.time.Instant
import java.util.*

/**
 * @author GrowlyX
 * @since 10/7/2021
 */
object CommentHandler
{

    /**
     * Attaches a [Comment] to a player.
     */
    fun addCommentToPlayer(
        issuer: Player, target: UUID, commentValue: String
    )
    {
        val comment = Comment(
            UUID.randomUUID(),
            issuer.uniqueId, target,
            Instant.now(),
            commentValue
        )

        DataStoreHandler.commentLayer.saveEntry(
            comment.uniqueId.toString(), comment
        ).thenRun {
            issuer.sendMessage("${CC.GREEN}You've attached a comment with the value ${CC.WHITE}$commentValue${CC.GREEN} to ${CC.WHITE}${CubedCacheUtil.fetchName(target)}'s${CC.GREEN} profile with the ID ${CC.YELLOW}#${SplitUtil.splitUuid(comment.uniqueId)}${CC.GREEN}.")
        }
    }

    /**
     * Removes a [Comment] from a player.
     *
     * [shortenedUniqueId] - the shortened version of
     * the comment unique id.
     */
    fun removeCommentFromPlayer(
        remover: Player,
        target: UUID,
        shortenedUniqueId: String
    )
    {
        DataStoreHandler.commentLayer.fetchAllEntriesWithFilter(
            Filters.eq("shortenedUniqueId", shortenedUniqueId)
        ).thenAccept {
            if (it.isEmpty()) {
                remover.sendMessage("${CC.RED}No comment with the id ${CC.YELLOW}#${shortenedUniqueId}${CC.RED} was found.")
                return@thenAccept
            }

            val first = it.values.first()!!

            DataStoreHandler.commentLayer.deleteEntry(
                first.uniqueId.toString()
            ).thenRun {
                remover.sendMessage("${CC.GREEN}You've removed a comment with the id ${CC.YELLOW}#${first.shortenedUniqueId}${CC.GREEN}.")
            }
        }
    }
}
