package gg.scala.lemon.player.wrapper

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
data class AsyncLemonPlayer(
    val future: CompletableFuture<LemonPlayer?>
)
{
    fun futureUsage(
        lambda: CompletableFuture<LemonPlayer?>.() -> Unit
    )
    {
        lambda.invoke(future)
    }

    companion object
    {
        @JvmStatic
        fun of(uniqueId: UUID): AsyncLemonPlayer
        {
            val online = Bukkit.getPlayer(uniqueId)

            return if (online != null)
            {
                AsyncLemonPlayer(
                    CompletableFuture.completedFuture(
                        PlayerHandler.findPlayer(uniqueId)
                            .orElse(null)
                    )
                )
            } else
            {
                AsyncLemonPlayer(
                    DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
                        .load(uniqueId, DataStoreStorageType.MONGO)
                )
            }
        }
    }
}
