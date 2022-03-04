package gg.scala.lemon.task

import gg.scala.lemon.Lemon
import gg.scala.store.storage.type.DataStoreStorageType
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

        Lemon.instance.serverLayer
            .save(instance, DataStoreStorageType.REDIS)
    }
}
