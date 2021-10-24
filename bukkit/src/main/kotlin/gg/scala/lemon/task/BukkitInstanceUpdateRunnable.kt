package gg.scala.lemon.task

import gg.scala.lemon.Lemon
import org.bukkit.Bukkit

object BukkitInstanceUpdateRunnable : Runnable
{

    override fun run()
    {
        val instance = Lemon.instance.localInstance

        instance.onlinePlayers = Bukkit.getOnlinePlayers().size
        instance.maxPlayers = Bukkit.getMaxPlayers()
        instance.whitelisted = Bukkit.hasWhitelist()
        instance.onlineMode = Bukkit.getOnlineMode()
        instance.version = Bukkit.getVersion()

        instance.ticksPerSecond = Lemon.instance.serverStatisticProvider.ticksPerSecond()
        instance.lastHeartbeat = System.currentTimeMillis()

        instance.metaData["init"] = Lemon.instance.init.toString()

        Lemon.instance.serverLayer.saveEntry(
            instance.serverId, instance
        ).whenComplete { _, u ->
            u?.printStackTrace()
        }
    }
}
