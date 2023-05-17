package gg.scala.lemon.player.sorter

import com.comphenix.packetwrapper.WrapperPlayServerPlayerInfo
import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketEvent
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import me.lucko.helper.protocol.Protocol
import java.util.function.Consumer

/**
 * @author GrowlyX
 * @since 5/17/2023
 */
@Service
object SortedRankPacketHandlerService : Consumer<PacketEvent>
{
    @Inject
    lateinit var plugin: Lemon

    @Configure
    fun configure()
    {
        if (!plugin.settings.tablistSortingEnabled)
        {
            plugin.logger.info("Skipping configuration of tablist sort packet handlers.")
            return
        }

        Protocol
            .subscribe(
                PacketType.Play.Server.PLAYER_INFO
            )
            .handler(this)
            .bindWith(plugin)
    }

    override fun accept(event: PacketEvent)
    {
        val packet = WrapperPlayServerPlayerInfo(event.packet)
        val newData = packet.data
            .sortedByDescending {
                val lemonPlayer = PlayerHandler
                    .find(it.profile.uuid)
                    ?: return@sortedByDescending -1

                val rank = lemonPlayer.disguiseRank()
                    ?: lemonPlayer.activeGrant?.getRank()
                    ?: return@sortedByDescending -1

                rank.weight
            }

        packet.data = newData
    }
}
