package com.solexgames.lemon.player.channel.impl

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.channel.Channel
import org.bukkit.entity.Player

class StaffChannel : Channel {

    override fun onMessage(player: Player, message: String) {
        // publish(player, message, channel)
    }

    override fun inChannel(player: Player): Boolean {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)
        var inChannel = false

        lemonPlayer.ifPresent {
            val metadata = it.getMetadata("staff-channel")

            inChannel = metadata != null
        }

        return inChannel
    }
}