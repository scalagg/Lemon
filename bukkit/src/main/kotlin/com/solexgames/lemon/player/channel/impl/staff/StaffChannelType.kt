package com.solexgames.lemon.player.channel.impl.staff

import org.bukkit.ChatColor

enum class StaffChannelType(var prefix: String, var color: ChatColor) {

    STAFF("#", ChatColor.AQUA),
    ADMIN("@", ChatColor.RED),
    MANAGER("$", ChatColor.DARK_RED),
    OWNER("!", ChatColor.BLUE)

}
