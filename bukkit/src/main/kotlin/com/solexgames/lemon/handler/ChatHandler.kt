package com.solexgames.lemon.handler

import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.player.channel.ChannelOverride
import com.solexgames.lemon.player.channel.impl.DefaultChannel
import com.solexgames.lemon.player.channel.impl.StaffChannel
import com.solexgames.lemon.player.channel.impl.StaffChannelType
import net.minecraft.util.com.google.common.primitives.Ints
import org.bukkit.entity.Player
import java.util.*

object ChatHandler {

    val channels = HashMap<String, Channel>()
    val channelOverrides = ArrayList<ChannelOverride>()

    var chatMuted = false
    var slowChatTime = 3

    init {
        registerChannel("default", DefaultChannel())

        for (value in StaffChannelType.values()) {
            registerChannel(value.name, StaffChannel(value))
        }
    }

    fun registerChannelOverride(channelOverride: ChannelOverride) {
        channelOverrides.add(channelOverride)
        channelOverrides.sortWith { a, b -> Ints.compare(b.getWeight(), a.getWeight()) }
    }

    fun findChannelOverride(player: Player): Optional<ChannelOverride> {
        var override: ChannelOverride? = null
        var index = 0

        while (override == null || !override.shouldOverride(player)) {
            override = channelOverrides[index++]

            if (index == channelOverrides.size) {
                break
            }
        }

        return Optional.ofNullable(override)
    }

    fun registerChannel(id: String, channel: Channel) {
        assert(!findChannel(id).isPresent)

        channels[id] = channel
    }

    fun findChannel(id: String): Optional<Channel> {
        return Optional.ofNullable(channels.getOrDefault(id, null))
    }
}
