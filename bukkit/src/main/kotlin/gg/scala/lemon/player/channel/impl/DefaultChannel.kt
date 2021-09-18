package gg.scala.lemon.player.channel.impl

import gg.scala.lemon.Lemon
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

open class DefaultChannel: Channel {

    override fun getId(): String {
        return "default"
    }

    override fun getFormatted(message: String, sender: String, rank: Rank, receiver: Player): String {
        val lemonPlayer = Lemon.instance.playerHandler.findOnlinePlayer(sender) ?: return ""
        val player = Bukkit.getPlayer(sender)

        return "${rank.prefix}${lemonPlayer.getColoredName()}${rank.suffix}${getChatTag(player) ?: ""}${CC.WHITE}: ${colorIfHasPermission(player, message).replace(receiver.name, "${CC.YELLOW}${receiver.name}${CC.RESET}")}"
    }

    open fun getChatTag(player: Player): String? {
        return ""
    }

    override fun getPrefix(): String? {
        return null
    }

    override fun getPermission(): String? {
        return null
    }

    override fun shouldCheckForPrefix(): Boolean {
        return false
    }

    override fun isGlobal(): Boolean {
        return false
    }
}
