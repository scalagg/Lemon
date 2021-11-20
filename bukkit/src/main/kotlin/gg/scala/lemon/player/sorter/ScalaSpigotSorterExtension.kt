package gg.scala.lemon.player.sorter

import gg.scala.lemon.player.event.impl.RankChangeEvent
import gg.scala.lemon.util.QuickAccess
import me.lucko.helper.Events
import net.minecraft.server.v1_8_R3.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author GrowlyX
 * @since 11/19/2021
 */
object ScalaSpigotSorterExtension
{
    fun initialLoad()
    {
        Events.subscribe(PlayerJoinEvent::class.java).handler {
            internalListSort()
        }
        Events.subscribe(RankChangeEvent::class.java).handler {
            internalListSort()
        }
    }

    private fun internalListSort()
    {
        MinecraftServer.getServer().playerList.sortPlayerList(
            Comparator.comparingInt { entity ->
                QuickAccess.realRank(Bukkit.getPlayer(entity.uniqueID)).weight
            }
        )
    }
}
