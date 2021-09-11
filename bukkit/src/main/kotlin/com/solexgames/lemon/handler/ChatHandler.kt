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

    val channels = mutableMapOf<String, Channel>()
    private val channelOverrides = ArrayList<ChannelOverride>()

    var chatMuted = false
    var slowChatTime = 0

    init {
        registerChannel("default", DefaultChannel())

        StaffChannelType.values().forEach {
            val staffChannel = StaffChannel(it)

            registerChannel(staffChannel.getId(), staffChannel)
        }
    }

    fun registerChannelOverride(channelOverride: ChannelOverride) {
        channelOverrides.add(channelOverride)
        channelOverrides.sortWith { a, b -> Ints.compare(b.getWeight(), a.getWeight()) }
    }

    fun findChannelOverride(player: Player): Optional<ChannelOverride> {
        var override: ChannelOverride? = null

        if (channelOverrides.isEmpty()) {
            return Optional.ofNullable(null)
        }

        var index = 0

        while (override == null || !override.shouldOverride(player)) {
            override = channelOverrides[index++]

            if (index == channelOverrides.size) {
                break
            }
        }

        return Optional.ofNullable(override)
    }

    private fun registerChannel(id: String, channel: Channel) {
        channels[id] = channel
    }

    fun findChannel(id: String): Channel? {
        return channels[id]
    }
}
