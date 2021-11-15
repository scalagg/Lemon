package gg.scala.lemon.adapter.placeholder.impl

import gg.scala.lemon.Lemon
import gg.scala.lemon.adapter.annotation.RequiredPlugin
import gg.scala.lemon.adapter.placeholder.PlaceholderAdapter
import gg.scala.lemon.handler.PlayerHandler
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/14/2021
 */
@RequiredPlugin("PlaceholderAPI")
class PlaceholderAPIExpansion : PlaceholderExpansion(), PlaceholderAdapter
{
    override fun getId() = "Placeholder API"

    override fun getIdentifier() = "lemon"
    override fun getAuthor() = "Scala"

    override fun getVersion(): String =
        Lemon.instance.description.version

    override fun onPlaceholderRequest(player: Player, params: String): String?
    {
        val lemonPlayer = PlayerHandler.findPlayer(player)
            .orElse(null) ?: return null
        val currentRank = lemonPlayer.activeGrant?.getRank() ?: return null

        return when (params.lowercase())
        {
            "rank_colored" -> currentRank.getColoredName()
            "rank_prefix" -> currentRank.prefix
            "rank_suffix" -> currentRank.suffix
            "rank_color" -> currentRank.color
            "custom_color" -> lemonPlayer.customColor()
            else -> null
        }
    }
}
