package gg.scala.lemon.task.daddyshark

import com.solexgames.daddyshark.commons.platform.DaddySharkPlatform
import gg.scala.lemon.Lemon
import org.bukkit.Bukkit

class BukkitInstanceUpdateRunnable(private var platform: DaddySharkPlatform): Runnable {

    override fun run() {
        val instance = platform.getLocalServerInstance()

        instance.onlinePlayers = Bukkit.getOnlinePlayers().size
        instance.maxPlayers = Bukkit.getMaxPlayers()
        instance.whitelisted = Bukkit.hasWhitelist()
        instance.onlineMode = Bukkit.getOnlineMode()
        instance.version = Bukkit.getVersion()



        instance.ticksPerSecond = Lemon.instance.serverStatisticProvider.ticksPerSecond()
        instance.lastHeartbeat = System.currentTimeMillis()

        platform.layer!!.saveEntry(instance.serverId, instance).whenComplete { _, u ->
            u?.printStackTrace()
        }
    }
}
