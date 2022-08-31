package gg.scala.lemon.software.task

import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.util.QuickAccess
import me.lucko.helper.promise.ThreadContext
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 9/8/2021
 */
@Repeating(20L, context = ThreadContext.ASYNC)
object ResourceUpdateRunnable : Runnable
{
    override fun run()
    {
        for (player in Bukkit.getOnlinePlayers())
        {
            val lemonPlayer = PlayerHandler.findPlayer(player)

            lemonPlayer.ifPresent {
                it.checkForGrantUpdate()
            }

            PunishmentHandler.fetchAllPunishmentsForTarget(player.uniqueId)
                .thenAccept {
                    it.forEach { punishment ->
                        QuickAccess.attemptExpiration(punishment)
                    }
                }
        }
    }
}
