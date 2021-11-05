package gg.scala.lemon.player.channel.impl.staff

import org.bukkit.ChatColor

enum class StaffChannelType(var prefix: String, var color: ChatColor) {

    STAFF("#", ChatColor.AQUA),
    ADMIN("@", ChatColor.RED),
    DEVELOPER("$", ChatColor.DARK_RED),
    MANAGER("!", ChatColor.BLUE)

}
