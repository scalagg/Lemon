package gg.scala.lemon.adapter.placeholder.impl

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import gg.scala.lemon.adapter.placeholder.PlaceholderAdapter
import gg.scala.lemon.handler.PlayerHandler
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/14/2021
 */
@Service
@IgnoreAutoScan
@SoftDependency("PlaceholderAPI")
class PlaceholderAPIExpansion : PlaceholderExpansion(), PlaceholderAdapter
{
    @Configure
    fun configure()
    {
        this.register()
    }

    override fun getId() = "Placeholder API"

    override fun getIdentifier() = "lemon"
    override fun getAuthor() = "Scala"

    override fun getVersion(): String =
        Lemon.instance.description.version

    override fun onPlaceholderRequest(
        player: Player, params: String
    ): String?
    {
        val lemonPlayer = PlayerHandler.findPlayer(player)
            .orElse(null) ?: return null

        val currentRank = lemonPlayer.activeGrant
            ?.getRank() ?: return null

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
