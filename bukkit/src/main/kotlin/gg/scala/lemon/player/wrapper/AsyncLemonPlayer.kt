package gg.scala.lemon.player.wrapper

import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.handler.DataStoreOrchestrator
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

            if (online != null)
            {
                val future = CompletableFuture<LemonPlayer?>()
                future.complete(
                    PlayerHandler.findPlayer(uniqueId)
                        .orElse(null)
                )

                return AsyncLemonPlayer(future)
            } else
            {
                return AsyncLemonPlayer(
                    DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
                        .load(uniqueId, DataStoreStorageType.MONGO)
                )
            }
        }
    }
}
