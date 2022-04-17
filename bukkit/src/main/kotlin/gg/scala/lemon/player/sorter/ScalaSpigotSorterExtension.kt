package gg.scala.lemon.player.sorter

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.disguise.update.event.PostDisguiseEvent
import gg.scala.lemon.disguise.update.event.UnDisguiseEvent
import gg.scala.lemon.util.QuickAccess
import me.lucko.helper.Events
import net.minecraft.server.v1_8_R3.EntityPlayer
import net.minecraft.server.v1_8_R3.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent

/**
 * @author GrowlyX
 * @since 11/19/2021
 */
object ScalaSpigotSorterExtension
{
    private val comparator: Comparator<EntityPlayer> =
        Comparator.comparingInt {
            QuickAccess.realRank(Bukkit.getPlayer(it.uniqueID)).weight
        }

    fun configure()
    {
        listOf(
            PlayerJoinEvent::class,
            PostDisguiseEvent::class,
            UnDisguiseEvent::class
        ).forEach {
            Events.subscribe(it.java)
                .handler { internalListSort() }
        }
    }

    private fun internalListSort()
    {
        MinecraftServer.getServer()
            .playerList.sortPlayerList(comparator)
    }
}
