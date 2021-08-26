package com.solexgames.lemon.player.channel

import org.bukkit.entity.Player

interface Channel {

    fun onMessage(player: Player, message: String)

    fun inChannel(player: Player): Boolean

}