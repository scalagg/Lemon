package gg.scala.lemon.player.nametag

import gg.scala.lemon.minequest
import gg.scala.lemon.player.sorter.TeamBasedSortStrategy
import gg.scala.lemon.util.QuickAccess.realRank
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object DefaultNametagProvider : NametagProvider("default", 10)
{
    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo
    {
        val rank = realRank(toRefresh)

        return createNametag(
            if (minequest()) "${
                if (ChatColor.stripColor(rank.prefix).isEmpty()) "" else "${rank.prefix} "
            }${rank.color}" else rank.color,
            "",
            TeamBasedSortStrategy.teamMappings[rank.uuid] ?: "z"
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
