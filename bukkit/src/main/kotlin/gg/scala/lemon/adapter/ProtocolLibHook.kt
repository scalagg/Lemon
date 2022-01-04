package gg.scala.lemon.adapter

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import gg.scala.lemon.listener.PlayerListener
import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 10/27/2021
 */
@Service(name = "protocol-lib")
@IgnoreAutoScan
object ProtocolLibHook
{
    @Configure
    fun configure()
    {
        try
        {
            val adapter = object :
                PacketAdapter(Lemon.instance, ListenerPriority.HIGHEST, PacketType.Play.Client.TAB_COMPLETE)
            {
                override fun onPacketReceiving(event: PacketEvent)
                {
                    if (event.packetType == PacketType.Play.Client.TAB_COMPLETE)
                    {
                        if (event.player.hasPermission("lemon.command-blacklist.bypass"))
                        {
                            return
                        }

                        val packet = event.packet
                        val message = packet.getSpecificModifier(String::class.java).read(0).lowercase()

                        val split = message.split(" ")

                        if (split.isEmpty())
                        {
                            return
                        }

                        val command = split[0]

                        if (command.contains(":") && command.endsWith(" "))
                        {
                            event.isCancelled = true
                            return
                        }

                        Lemon.instance.settings.blacklistedCommands.forEach {
                            if (command.equals("/$it", true))
                            {
                                event.isCancelled = true
                                return
                            }
                        }
                    }
                }
            }

            ProtocolLibrary.getProtocolManager().addPacketListener(adapter)
        } catch (ignored: Exception)
        {

        }
    }
}
