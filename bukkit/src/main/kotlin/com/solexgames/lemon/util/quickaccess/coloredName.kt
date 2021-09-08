package com.solexgames.lemon.util.quickaccess

import com.solexgames.lemon.Lemon
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
fun coloredName(player: Player): String {
    val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

    return lemonPlayer.getColoredName()
}
