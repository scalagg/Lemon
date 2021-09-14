package com.solexgames.lemon.task

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.QuickAccess
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 9/8/2021
 */
class ResourceUpdateRunnable : Runnable {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)

            lemonPlayer.ifPresent {
                it.checkForGrantUpdate()
            }

            Lemon.instance.punishmentHandler.fetchAllPunishmentsForTarget(player.uniqueId).thenAccept {
                it.forEach { punishment -> QuickAccess.attemptExpiration(punishment) }
            }
        }
    }
}
