package gg.scala.lemon.player.color

import org.bukkit.ChatColor
import org.bukkit.Color

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
data class PlayerColor(
    val chatColor: ChatColor,
    val bukkitColor: Color,
    val name: String
)
