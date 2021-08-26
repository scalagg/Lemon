package com.solexgames.lemon.handler

import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.player.channel.impl.DefaultChannel
import com.solexgames.lemon.player.channel.impl.StaffChannel
import com.solexgames.lemon.player.channel.impl.StaffChannelType

object ChatHandler {

    private val channels: MutableMap<String, Channel> = mutableMapOf()

    init {
        channels["default"] = DefaultChannel()

        for (value in StaffChannelType.values()) {
            channels[value.name] = StaffChannel(value)
        }
    }
}
