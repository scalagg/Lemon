package gg.scala.lemon.player.nametag.rainbow

import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
object RainbowNametagProvider : NametagProvider(
    "rainbow", 1000
)
{
    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo?
    {
        if (!RainbowNametagHandler.rainbowNametagEnabled.contains(toRefresh.uniqueId))
        {
            return null
        }

        return RainbowNametagHandler.nametagInfo
    }
}