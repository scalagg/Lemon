package com.solexgames.lemon.player.channel.impl

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

class StaffChannel(private val channel: StaffChannelType): Channel {

    override fun getId(): String {
        return channel.name.toLowerCase()
    }

    override fun getPermission(): String {
        return "lemon.channel.${channel.name.toLowerCase()}"
    }

    override fun getFormatted(message: String, sender: String, rank: Rank, receiver: Player): String {
        return "${channel.color}[${channel.name[0]}] ${CC.DARK_AQUA}[${Lemon.instance.settings.id}] ${rank.color}${sender}${CC.AQUA}: ${dePrefixed(message)}"
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
