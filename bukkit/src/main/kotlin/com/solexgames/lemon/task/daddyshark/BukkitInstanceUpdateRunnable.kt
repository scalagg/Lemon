package com.solexgames.lemon.task.daddyshark

import com.solexgames.daddyshark.commons.platform.DaddySharkPlatform
import com.solexgames.daddyshark.commons.update.InstanceUpdateRunnable
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import org.bukkit.Bukkit

class BukkitInstanceUpdateRunnable(private var platform: DaddySharkPlatform): Runnable {

    override fun run() {
        val instance = platform.getLocalServerInstance()

        instance.maxPlayers = Bukkit.getOnlinePlayers().size
        instance.onlinePlayers = Bukkit.getMaxPlayers()
        instance.whitelisted = Bukkit.hasWhitelist()
        instance.onlineMode = Bukkit.getOnlineMode()
        instance.version = Bukkit.getVersion()

        instance.ticksPerSecond = SparkProvider.get().tps()?.poll(StatisticWindow.TicksPerSecond.MINUTES_1) ?: 0.0
        instance.lastHeartbeat = System.currentTimeMillis()

        platform.layer!!.saveEntry(instance.serverId, instance).whenComplete { _, u ->
            u?.printStackTrace()
        }
    }
}
