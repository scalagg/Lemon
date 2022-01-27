package gg.scala.lemon.handler

import com.mongodb.client.model.Filters
import gg.scala.lemon.player.comment.Comment
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.SplitUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/7/2021
 */
object CommentHandler
{
    fun fetchComments(target: UUID): CompletableFuture<Map<UUID, Comment>>
    {
        val controller = DataStoreObjectControllerCache.findNotNull<Comment>()

        return controller.useLayerWithReturn<MongoDataStoreStorageLayer<Comment>, CompletableFuture<Map<UUID, Comment>>>(
            DataStoreStorageType.MONGO
        ) {
            return@useLayerWithReturn this.loadAllWithFilter(
                Filters.eq("target", target.toString())
            )
        }
    }

    /**
     * Attaches a [Comment] to a player.
     */
    fun addCommentToPlayer(
        issuer: Player, target: UUID,
        commentValue: String
    )
    {
        val comment = Comment(
            UUID.randomUUID(),
            issuer.uniqueId, target,
            Date(), commentValue
        )

        DataStoreObjectControllerCache.findNotNull<Comment>()
            .save(comment, DataStoreStorageType.MONGO)

        issuer.sendMessage(
            "${CC.GREEN}You've attached a comment with the value ${CC.WHITE}$commentValue${CC.GREEN} to ${CC.WHITE}${
                CubedCacheUtil.fetchName(
                    target
                )
            }'s${CC.GREEN} profile with the ID ${CC.YELLOW}#${SplitUtil.splitUuid(comment.uniqueId)}${CC.GREEN}."
        )
    }

    /**
     * Removes a [Comment] from a player.
     *
     * [shortenedUniqueId] - the shortened version of
     * the comment unique id.
     */
    fun removeCommentFromPlayer(
        remover: Player,
        shortenedUniqueId: String
    )
    {
        val controller = DataStoreObjectControllerCache.findNotNull<Comment>()

        controller.useLayer<MongoDataStoreStorageLayer<Comment>>(
            DataStoreStorageType.MONGO
        ) {
            this.loadAllWithFilter(
                Filters.eq("shortenedUniqueId", shortenedUniqueId)
            ).thenAccept {
                if (it.isEmpty())
                {
                    remover.sendMessage("${CC.RED}No comment with the id ${CC.YELLOW}#${shortenedUniqueId}${CC.RED} was found.")
                    return@thenAccept
                }

                val first = it.values.first()

                DataStoreObjectControllerCache.findNotNull<Comment>()
                    .delete(first.uniqueId, DataStoreStorageType.MONGO)
                    .thenRun {
                        remover.sendMessage("${CC.GREEN}You've removed a comment with the id ${CC.YELLOW}#${first.shortenedUniqueId}${CC.GREEN}.")
                    }
            }
        }
    }
}
