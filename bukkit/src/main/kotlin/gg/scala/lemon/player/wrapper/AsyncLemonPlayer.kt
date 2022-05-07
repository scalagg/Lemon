package gg.scala.lemon.player.wrapper

import com.mongodb.client.model.Filters
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.SplitUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.commons.acf.ConditionFailedException
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
data class AsyncLemonPlayer(
    val uniqueId: UUID,
    val future: CompletableFuture<List<LemonPlayer>>
)
{
    fun validatePlayers(
        sender: CommandSender,
        ignoreEmpty: Boolean,
        lambda: (LemonPlayer) -> Unit
    ): CompletableFuture<Void>
    {
        return future.thenAccept {
            if (it.isEmpty())
            {
                val username = CubedCacheUtil
                    .fetchName(this.uniqueId)

                if (ignoreEmpty && username != null)
                {
                    lambda.invoke(LemonPlayer(
                        this.uniqueId,
                        username,
                        null
                    ))

                    return@thenAccept
                }

                throw ConditionFailedException("No user entry matching the username ${CC.YELLOW}$username${CC.RED} was found.")
            }

            if (it.size > 1)
            {
                val sortedByLastLogin = it
                    .sortedByDescending { lemonPlayer ->
                        lemonPlayer
                            .getMetadata("last-connection")
                            ?.asString()?.toLong() ?: 0L
                    }

                val bestChoice = sortedByLastLogin.first()

                if (sender is Player)
                {
                    val fancyMessage = FancyMessage()
                        .withMessage(
                            "",
                            "${CC.D_RED}Multiple accounts with that name were found.",
                            "${CC.RED}Click one of the following messages to copy their unique id."
                        )

                    for (lemonPlayer in it)
                    {
                        fancyMessage
                            .withMessage(
                                "\n${CC.GRAY}  - ${
                                    SplitUtil.splitUuid(lemonPlayer.uniqueId)
                                }${
                                    if (bestChoice.uniqueId == lemonPlayer.uniqueId)
                                    {
                                        " ${CC.I_WHITE}(best choice)"
                                    } else ""
                                }"
                            )
                            .andHoverOf(
                                "${CC.YELLOW}Click to copy their unique id."
                            )
                            .andCommandOf(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                lemonPlayer.uniqueId.toString()
                            )
                    }

                    fancyMessage.withMessage("\n")
                    fancyMessage.sendToPlayer(sender)
                } else
                {
                    sender.sendMessage(arrayOf(
                        "${CC.D_RED}Multiple accounts with that name were found."
                    ))

                    for (lemonPlayer in it)
                    {
                        sender.sendMessage("${CC.GRAY}  - ${
                            SplitUtil.splitUuid(lemonPlayer.uniqueId)
                        }${
                            if (bestChoice.uniqueId == lemonPlayer.uniqueId)
                            {
                                " ${CC.I_WHITE}(best choice)"
                            } else ""
                        }")
                    }
                }

                return@thenAccept
            }

            lambda.invoke(it[0])
        }
    }

    companion object
    {
        @JvmStatic
        fun of(uniqueId: UUID, forcefullySpecified: Boolean): AsyncLemonPlayer
        {
            val online = Bukkit.getPlayer(uniqueId)

            return if (online != null)
            {
                val player = PlayerHandler
                    .findPlayer(uniqueId)
                    .orElse(null)
                    ?: return AsyncLemonPlayer(
                        uniqueId,
                        CompletableFuture
                            .completedFuture(listOf())
                    )

                AsyncLemonPlayer(
                    uniqueId,
                    CompletableFuture.completedFuture(
                        listOf(player)
                    )
                )
            } else
            {
                AsyncLemonPlayer(
                    uniqueId,
                    DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
                        .useLayerWithReturn<MongoDataStoreStorageLayer<LemonPlayer>, CompletableFuture<List<LemonPlayer>>>(
                            DataStoreStorageType.MONGO
                        ) {
                            if (forcefullySpecified)
                            {
                                return@useLayerWithReturn this.load(uniqueId)
                                    .thenApply {
                                        if (it == null)
                                            return@thenApply listOf()

                                        listOf(it)
                                    }
                            }

                            val username = CubedCacheUtil
                                .fetchName(uniqueId)

                            return@useLayerWithReturn this.loadAllWithFilter(
                                // praying this never throws an exception.
                                Filters.eq("name", username)
                            ).thenApply {
                                it.values.toList()
                            }
                        }
                )
            }
        }
    }
}
