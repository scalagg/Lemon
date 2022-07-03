package gg.scala.lemon.task

import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.lemon.Lemon
import gg.scala.lemon.discovery.LemonDiscoveryClient
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.promise.ThreadContext
import org.bukkit.Bukkit

@Repeating(20L, context = ThreadContext.ASYNC)
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

        LemonDiscoveryClient.discovery()
            .agentClient()
            .pass(Lemon.instance.settings.id)
    }
}
