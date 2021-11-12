package gg.scala.lemon.player.color

import org.bukkit.ChatColor
import org.bukkit.Color

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
object PlayerColorHandler
{
    val colorPairs = mutableListOf<PlayerColor>()

    fun find(id: String): PlayerColor? = colorPairs
        .firstOrNull { it.name.equals(id, true) }

    fun initialLoad()
    {
        initialize(
            PlayerColor(ChatColor.RED, Color.fromRGB(255, 85, 85), "Red"),
            PlayerColor(ChatColor.BLUE, Color.fromRGB(85, 85, 255), "Blue"),
            PlayerColor(ChatColor.AQUA, Color.fromRGB(85, 255, 255), "Light Blue"),
            PlayerColor(ChatColor.DARK_AQUA, Color.fromRGB(0, 170, 170), "Cyan"),
            PlayerColor(ChatColor.DARK_PURPLE, Color.fromRGB(170, 0, 170), "Purple"),
            PlayerColor(ChatColor.LIGHT_PURPLE, Color.fromRGB(255, 85, 255), "Pink"),
            PlayerColor(ChatColor.DARK_GREEN, Color.fromRGB(0, 170, 0), "Green"),
            PlayerColor(ChatColor.GREEN, Color.fromRGB(85, 255, 85), "Lime")
        )
    }

    fun initialize(vararg color: PlayerColor)
    {
        for (playerColor in color)
        {
            colorPairs.add(playerColor)
        }
    }
}
