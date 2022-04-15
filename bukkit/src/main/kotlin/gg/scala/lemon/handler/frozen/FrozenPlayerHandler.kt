package gg.scala.lemon.handler.frozen

import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.lemon.Lemon
import gg.scala.lemon.util.other.Expirable
import me.lucko.helper.promise.ThreadContext
import org.bukkit.Bukkit
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
@Repeating(100L, context = ThreadContext.ASYNC)
object FrozenPlayerHandler : Runnable
{
    val expirables = mutableMapOf<UUID, FrozenExpirable>()

    override fun run()
    {
        Bukkit.getOnlinePlayers()
            .filter { it.hasMetadata("frozen") }
            .forEach {
                val expirable = expirables[it.uniqueId]

                if (expirable == null)
                {
                    it.sendMessage(
                        Lemon.instance.languageConfig.frozenPlayerTimeIsUpMessage
                    )
                } else
                {
                    it.sendMessage(
                        Lemon.instance.languageConfig.frozenPlayerHasTimeMessage
                            .replace("%s", expirable.durationFromNowStringRaw)
                    )
                }
            }
    }

    class FrozenExpirable : Expirable(
        System.currentTimeMillis(),
        TimeUnit.MINUTES.toMillis(5)
    )
}
