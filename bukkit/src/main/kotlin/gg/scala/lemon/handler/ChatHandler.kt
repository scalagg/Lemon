package gg.scala.lemon.handler

import com.google.common.primitives.Ints
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.channel.ChannelOverride
import gg.scala.lemon.player.channel.impl.DefaultChannel
import gg.scala.lemon.player.channel.impl.staff.StaffChannel
import gg.scala.lemon.player.channel.impl.staff.StaffChannelType
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*

@Service
object ChatHandler {

    val channels = mutableMapOf<String, Channel>()
    val chatChecks = mutableListOf<(AsyncPlayerChatEvent) -> Pair<String, Boolean>>()

    private val channelOverrides = mutableListOf<ChannelOverride>()

    var chatMuted = false
    var slowChatTime = 0

    @Configure
    fun configure()
    {
        registerChannel("default", DefaultChannel())

        StaffChannelType.values().forEach {
            val staffChannel = StaffChannel(it)

            registerChannel(staffChannel.getId(), staffChannel)
        }
    }

    @Close
    fun close()
    {
        channels.clear()
        chatChecks.clear()
    }

    fun registerChatCheck(
        lambda: (AsyncPlayerChatEvent) -> Pair<String, Boolean>
    )
    {
        chatChecks.add(lambda)
    }

    fun registerChannelOverride(channelOverride: ChannelOverride) {
        channelOverrides.add(channelOverride)
        channelOverrides.sortWith { a, b -> Ints.compare(b.getWeight(), a.getWeight()) }
    }

    fun findChannelOverride(player: Player): Optional<ChannelOverride> {
        return Optional.ofNullable(
            channelOverrides
                .sortedByDescending { it.getWeight() }
                .firstOrNull { it.shouldOverride(player) }
        )
    }

    private fun registerChannel(id: String, channel: Channel) {
        channels[id] = channel
    }

    fun findChannel(id: String): Channel? {
        return channels[id]
    }
}
