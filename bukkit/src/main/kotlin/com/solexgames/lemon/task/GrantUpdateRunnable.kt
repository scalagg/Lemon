package com.solexgames.lemon.task

import com.solexgames.lemon.Lemon
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 9/8/2021
 */
class GrantUpdateRunnable : Runnable {

    override fun run() {
        Bukkit.getOnlinePlayers().forEach { player ->
            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)

            lemonPlayer.ifPresent {
                it.recalculateGrants()
            }
        }
    }
}
