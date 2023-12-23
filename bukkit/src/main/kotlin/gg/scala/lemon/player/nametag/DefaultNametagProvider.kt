package gg.scala.lemon.player.nametag

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.internal.ExtHookIns
import gg.scala.lemon.minequest
import gg.scala.lemon.player.sorter.SortedRankCache
import gg.scala.lemon.util.QuickAccess.realRank
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object DefaultNametagProvider : NametagProvider("default", 10)
{
    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo?
    {
        val lemonPlayer = PlayerHandler
            .find(toRefresh.uniqueId)
            ?: return null

        val rank = lemonPlayer.disguiseRank() ?: realRank(toRefresh)
        val sortMapping = SortedRankCache.teamMappings[rank.uuid]

        return createNametag(
            if (minequest()) "${
                if (ChatColor.stripColor(rank.prefix).isEmpty()) "" else ExtHookIns.playerRankPrefix(toRefresh, rank, lemonPlayer)
            }${
                if (minequest() && rank.name == "Platinum") "" else rank.color
            }" else rank.color,
            "",
            if (Lemon.instance.settings.tablistSortingEnabled)
            {
                if (sortMapping != null && minequest())
                    sortMapping + ExtHookIns.playerRankColorType(toRefresh, rank, lemonPlayer)
                else sortMapping ?: "z"
            } else
            {
                Lemon.instance.settings.tabWeight
            }
        )
    }
}
