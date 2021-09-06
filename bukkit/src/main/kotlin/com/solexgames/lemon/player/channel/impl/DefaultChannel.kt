package com.solexgames.lemon.player.channel.impl

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class DefaultChannel: Channel {

    override fun getId(): String {
        return "default"
    }

    override fun getFormatted(message: String, sender: String, rank: Rank, receiver: Player): String {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(sender).orElse(null)
        val player = Bukkit.getPlayer(sender)

        return "${rank.getPrefix()}${rank.color}${lemonPlayer.name}${CC.WHITE}: ${colorIfHasPermission(player, message)}"
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
