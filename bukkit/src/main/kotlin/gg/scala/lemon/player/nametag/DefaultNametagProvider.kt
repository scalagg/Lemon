package gg.scala.lemon.player.nametag

import gg.scala.lemon.channel.channels.DefaultChatChannel
import gg.scala.lemon.minequest
import gg.scala.lemon.util.QuickAccess.realRank
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor
import org.bukkit.entity.Player

object DefaultNametagProvider : NametagProvider("default", 10)
{
    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo
    {
        return createNametag(
            realRank(toRefresh).let {
                if (minequest()) "${
                    if (ChatColor.stripColor(it.prefix).isEmpty()) "" else "${it.prefix} "
                }${it.color}" else it.color
            },
            if (minequest())
            {
                "${CC.RESET}${
                    DefaultChatChannel.serializer
                        .serialize(
                            DefaultChatChannel
                                .chatTagProvider
                                .invoke(toRefresh)
                        )
                        .removeSuffix(" ")
                }"
            } else ""
        )
    }
}
