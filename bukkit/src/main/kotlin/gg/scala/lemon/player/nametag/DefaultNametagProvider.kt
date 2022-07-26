package gg.scala.lemon.player.nametag

import gg.scala.lemon.minequest
import gg.scala.lemon.util.QuickAccess.realRank
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.entity.Player

object DefaultNametagProvider : NametagProvider("default", 10)
{
    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo
    {
        return createNametag(
            realRank(toRefresh).let {
                if (minequest()) "${it.prefix} ${it.color}" else it.color
            }, ""
        )
    }
}
