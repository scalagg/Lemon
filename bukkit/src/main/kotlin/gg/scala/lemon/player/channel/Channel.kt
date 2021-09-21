package gg.scala.lemon.player.channel

import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.type.Permissible
import gg.scala.lemon.util.type.Prefixable
import org.bukkit.ChatColor
import org.bukkit.entity.Player

interface Channel: Prefixable, Permissible<Player> {

    fun getId(): String

    fun isGlobal(): Boolean

    fun getFormatted(message: String, sender: String, rank: Rank, receiver: Player): String

    override fun hasPermission(t: Player): Boolean {
        if (getPermission() == null) {
            return true
        }

        return t.hasPermission(getPermission())
    }

    override fun isPrefixed(message: String): Boolean {
        val prefix = getPrefix()

        return prefix != null && message.startsWith("$prefix ")
    }

    fun dePrefixed(message: String): String {
        val prefix = getPrefix() ?: return message

        return message.removePrefix("$prefix ")
    }

    fun colorIfHasPermission(player: Player, message: String): String {
        val hasPermission = player.hasPermission("lemon.chat.colors")

        if (!hasPermission) {
            return message
        }

        return ChatColor.translateAlternateColorCodes('&', message)
    }

}
