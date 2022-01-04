package gg.scala.lemon.player.sorter

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.disguise.update.event.PostDisguiseEvent
import gg.scala.lemon.disguise.update.event.UnDisguiseEvent
import gg.scala.lemon.player.event.impl.RankChangeEvent
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.realRank
import me.lucko.helper.Events
import net.evilblock.cubed.util.bukkit.Tasks
import net.minecraft.server.v1_8_R3.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author GrowlyX
 * @since 11/19/2021
 */
@Service
object ScalaSpigotSorterExtension
{
    @Configure
    fun configure()
    {
        Events.subscribe(PlayerJoinEvent::class.java).handler {
            asyncInternalListSort()
        }
        Events.subscribe(RankChangeEvent::class.java).handler {
            asyncInternalListSort()
        }

        Events.subscribe(PostDisguiseEvent::class.java).handler {
            asyncInternalListSort()
        }
        Events.subscribe(UnDisguiseEvent::class.java).handler {
            asyncInternalListSort()
        }
    }

    private fun asyncInternalListSort()
    {
        Tasks.async {
            MinecraftServer.getServer().playerList.sortPlayerList(
                Comparator.comparingInt { entity ->
                    realRank(Bukkit.getPlayer(entity.uniqueID)).weight
                }
            )
        }
    }
}
