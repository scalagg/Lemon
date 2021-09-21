package gg.scala.lemon.handler

import com.google.common.primitives.Ints
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.channel.ChannelOverride
import gg.scala.lemon.player.channel.impl.DefaultChannel
import gg.scala.lemon.player.channel.impl.staff.StaffChannel
import gg.scala.lemon.player.channel.impl.staff.StaffChannelType
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
