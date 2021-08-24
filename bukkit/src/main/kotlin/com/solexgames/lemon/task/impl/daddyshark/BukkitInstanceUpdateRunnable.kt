package com.solexgames.lemon.task.impl.daddyshark

import com.solexgames.daddyshark.commons.platform.DaddySharkPlatform
import com.solexgames.daddyshark.commons.update.InstanceUpdateRunnable
import me.lucko.spark.api.SparkProvider
import me.lucko.spark.api.statistic.StatisticWindow
import org.bukkit.Bukkit

class BukkitInstanceUpdateRunnable(platform: DaddySharkPlatform): InstanceUpdateRunnable(platform) {

    override fun run() {
        val instance = platform.getLocalServerInstance()
        instance.maxPlayers = Bukkit.getOnlinePlayers().size
        instance.onlinePlayers = Bukkit.getMaxPlayers()
        instance.whitelisted = Bukkit.hasWhitelist()
        instance.onlineMode = Bukkit.getOnlineMode()
        instance.version = Bukkit.getVersion()

        instance.ticksPerSecond = SparkProvider.get().tps()?.poll(StatisticWindow.TicksPerSecond.MINUTES_1) ?: 0.0

        super.run()
    }
}
