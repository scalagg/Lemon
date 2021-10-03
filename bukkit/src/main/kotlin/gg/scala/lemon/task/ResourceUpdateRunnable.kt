package gg.scala.lemon.task

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.util.QuickAccess
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 9/8/2021
 */
class ResourceUpdateRunnable : Runnable {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val lemonPlayer = PlayerHandler.findPlayer(player)

            lemonPlayer.ifPresent {
                it.checkForGrantUpdate()
            }

            PunishmentHandler.fetchAllPunishmentsForTarget(player.uniqueId).thenAccept {
                it.forEach { punishment -> QuickAccess.attemptExpiration(punishment) }
            }
        }

        System.gc()
    }
}
