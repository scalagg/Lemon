package gg.scala.lemon.feature

import com.comphenix.protocol.PacketType
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import me.lucko.helper.protocol.Protocol

/**
 * @author GrowlyX
 * @since 10/27/2021
 */
@Service
@IgnoreAutoScan
@SoftDependency("ProtocolLib")
object ProtocolLibFeature
{
    @Configure
    fun configure()
    {
        Protocol
            .subscribe(PacketType.Play.Client.TAB_COMPLETE)
            .handler { event ->
                if (event.packetType == PacketType.Play.Client.TAB_COMPLETE)
                {
                    if (event.player.hasPermission("lemon.command-blacklist.bypass"))
                    {
                        return@handler
                    }

                    val packet = event.packet
                    val message = packet
                        .getSpecificModifier(String::class.java)
                        .read(0).lowercase()

                    val split = message.split(" ")

                    if (split.isEmpty())
                    {
                        return@handler
                    }

                    val command = split[0]

                    if (
                        command.contains(":") &&
                        !command.endsWith(":")
                    )
                    {
                        event.isCancelled = true
                        return@handler
                    }

                    Lemon.instance.settings.blacklistedCommands.forEach {
                        if (command.equals("/$it", true))
                        {
                            event.isCancelled = true
                            return@handler
                        }
                    }
                }
            }
            .bindWith(Lemon.instance)
    }
}
