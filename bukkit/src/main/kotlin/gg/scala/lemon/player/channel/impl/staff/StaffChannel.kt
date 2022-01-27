package gg.scala.lemon.player.channel.impl.staff

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

class StaffChannel(private val channel: StaffChannelType): Channel {

    override fun getId(): String {
        return channel.name.lowercase()
    }

    override fun getPermission(): String {
        return "lemon.channel.${channel.name.lowercase()}"
    }

    override fun hasPermission(t: Player): Boolean {
        val lemonPlayer = PlayerHandler.findPlayer(t).orElse(null)

        return if (lemonPlayer != null) {
            lemonPlayer.hasPermission(getPermission()) && !lemonPlayer.getSetting("staff-messages-disabled")
        } else false
    }

    override fun getFormatted(message: String, sender: String, rank: Rank, receiver: Player): String {
        return "${channel.color}[${channel.name[0]}] ${CC.D_AQUA}[%s] ${rank.color}${sender}${CC.WHITE}: ${CC.AQUA}${dePrefixed(message)}"
    }

    override fun isGlobal(): Boolean {
        return true
    }

    override fun shouldCheckForPrefix(): Boolean {
        return true
    }

    override fun getPrefix(): String {
        return channel.prefix
    }

}
