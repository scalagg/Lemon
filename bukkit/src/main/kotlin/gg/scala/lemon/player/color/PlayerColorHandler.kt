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
            PlayerColor(ChatColor.RED, Color.RED, "Red"),
            PlayerColor(ChatColor.BLUE, Color.BLUE, "Blue"),
            PlayerColor(ChatColor.AQUA, Color.AQUA, "Light Blue"),
            PlayerColor(ChatColor.DARK_AQUA, Color.fromRGB(0, 255, 255), "Cyan"),
            PlayerColor(ChatColor.DARK_PURPLE, Color.PURPLE, "Purple"),
            PlayerColor(ChatColor.LIGHT_PURPLE, Color.fromRGB(255,20,147), "Pink"),
            PlayerColor(ChatColor.DARK_GREEN, Color.GREEN, "Green"),
            PlayerColor(ChatColor.GREEN, Color.LIME, "Lime")
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
