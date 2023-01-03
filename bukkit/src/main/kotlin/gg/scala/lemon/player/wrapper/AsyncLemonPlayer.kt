package gg.scala.lemon.player.wrapper

import com.mongodb.client.model.Filters
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.commons.acf.BukkitCommandExecutionContext
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.SplitUtil
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
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
    val uniqueIdCompute: () -> Pair<UUID, Boolean>,
    val future: (Pair<UUID, Boolean>) -> CompletableFuture<List<LemonPlayer>>
)
{
    fun computeNow() = future(uniqueIdCompute())

    fun validatePlayers(
        sender: CommandSender,
        ignoreEmpty: Boolean,
        lambda: (LemonPlayer) -> Unit
    ): CompletableFuture<Void>
    {
        return CompletableFuture
            .supplyAsync {
                uniqueIdCompute()
            }
            .thenComposeAsync {
                future.invoke(it)
                    .thenApply { result ->
                        it to result
                    }
            }
            .thenAcceptAsync {
                if (it.second.isEmpty())
                {
                    val username = CubedCacheUtil
                        .fetchName(it.first.first)

                    if (ignoreEmpty && username != null)
                    {
                        lambda.invoke(
                            LemonPlayer(
                                it.first.first,
                                null
                            )
                        )

                        return@thenAcceptAsync
                    }

                    throw ConditionFailedException("No user entry matching the username ${CC.YELLOW}$username${CC.RED} was found.")
                }

                if (it.second.size > 1)
                {
                    val sortedByLastLogin = it.second
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

                        for (lemonPlayer in it.second)
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
                        sender.sendMessage(
                            arrayOf(
                                "${CC.D_RED}Multiple accounts with that name were found."
                            )
                        )

                        for (lemonPlayer in it.second)
                        {
                            sender.sendMessage(
                                "${CC.GRAY}  - ${
                                    SplitUtil.splitUuid(lemonPlayer.uniqueId)
                                }${
                                    if (bestChoice.uniqueId == lemonPlayer.uniqueId)
                                    {
                                        " ${CC.I_WHITE}(best choice)"
                                    } else ""
                                }"
                            )
                        }
                    }

                    return@thenAcceptAsync
                }

                lambda.invoke(it.second[0])
            }
    }

    companion object
    {
        @JvmStatic
        fun of(
            uniqueId: UUID?, context: BukkitCommandExecutionContext? = null
        ): AsyncLemonPlayer
        {
            val online = Bukkit.getPlayer(uniqueId)

            if (uniqueId == null && context == null)
            {
                throw IllegalArgumentException(
                    "Both BukkitCommandExecutionContext and uniqueId is null. There is no way of deriving a player attribute."
                )
            }

            var actualUniqueId = ctx@{
                if (uniqueId != null)
                    // fallback to true for faster lookups when searching for alt accounts
                    return@ctx uniqueId to true

                // context must not be null at this point
                Lemon.instance.parseUniqueIdFromContext(context!!)
            }

            return if (online != null)
            {
                val player = PlayerHandler
                    .findPlayer(online.uniqueId)
                    .orElse(null)
                    ?: return AsyncLemonPlayer(actualUniqueId) {
                        CompletableFuture
                            .completedFuture(listOf())
                    }

                AsyncLemonPlayer(actualUniqueId) {
                    CompletableFuture.completedFuture(listOf(player))
                }
            } else
            {
                AsyncLemonPlayer(actualUniqueId) {
                    DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
                        .useLayerWithReturn<MongoDataStoreStorageLayer<LemonPlayer>, CompletableFuture<List<LemonPlayer>>>(
                            DataStoreStorageType.MONGO
                        ) {
                            if (it.second)
                            {
                                return@useLayerWithReturn this
                                    .load(it.first)
                                    .thenApply { copy ->
                                        if (copy == null)
                                            return@thenApply listOf()

                                        listOf(copy)
                                    }
                            }

                            return@useLayerWithReturn CompletableFuture
                                .supplyAsync {
                                    ScalaStoreUuidCache.username(it.first)
                                }
                                .thenComposeAsync { username ->
                                    loadAllWithFilter(
                                        Filters.eq("name", username ?: "")
                                    ).thenApply { mappings ->
                                        mappings.values.toList()
                                    }
                                }
                        }
                }
            }
        }
    }
}
