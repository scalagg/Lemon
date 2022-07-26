package gg.scala.lemon.util.minequest.platinum

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 7/25/2022
 */
data class MinequestPlatinumColor(
    val paneColor: Int,
    val menuPosition: Int,
    val colorName: String,
    val translated: String,
    val chatColor: ChatColor
)
