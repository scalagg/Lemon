package gg.scala.lemon.player.nametag

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.internal.ExtHookIns
import gg.scala.lemon.minequest
import gg.scala.lemon.player.sorter.TeamBasedSortStrategy
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
        val thing2 = TeamBasedSortStrategy.teamMappings[rank.uuid]

        return createNametag(
            if (minequest()) "${
                if (ChatColor.stripColor(rank.prefix).isEmpty()) "" else ExtHookIns.playerRankPrefix(toRefresh, rank, lemonPlayer)
            }${
                if (minequest() && rank.name == "Platinum") "" else rank.color
            }" else rank.color,
            "",
            if (thing2 != null && minequest())
                thing2 + ExtHookIns.playerRankColorType(toRefresh, rank, lemonPlayer)
            else thing2 ?: "z"
//            if (minequest())
//            {
//                "${CC.WHITE}${
//                    DefaultChatChannel.serializer
//                        .serialize(
//                            DefaultChatChannel
//                                .chatTagProvider
//                                .invoke(toRefresh)
//                        )
//                        .removeSuffix(" ")
//                }"
//            } else ""
        )
    }
}
