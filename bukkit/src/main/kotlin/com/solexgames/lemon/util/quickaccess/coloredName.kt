package com.solexgames.lemon.util.quickaccess

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
fun coloredNameOrConsole(sender: CommandSender): String {
    val lemonPlayer = sender.name?.let { Lemon.instance.playerHandler.findPlayer(it).orElse(null) }

    lemonPlayer?.let {
        return it.getColoredName()
    } ?: return "${CC.D_RED}Console"
}

fun coloredName(name: String?): String? {
    val lemonPlayer = name?.let { Lemon.instance.playerHandler.findPlayer(it).orElse(null) }

    lemonPlayer?.let {
        return it.getColoredName()
    } ?: return name
}

fun coloredName(player: Player): String? {
    return coloredName(player.name)
}
