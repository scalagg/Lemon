package gg.scala.lemon.player.spatial.tablist

import gg.scala.aware.AwareBuilder
import gg.scala.aware.codec.codecs.interpretation.AwareMessageCodec
import gg.scala.aware.message.AwareMessage
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.spatial.tablist.events.TabSyncSubscribeEvent
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import me.lucko.helper.terminable.Terminable
import me.lucko.helper.terminable.composite.CompositeTerminable
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Redis-based tab list synchronization service that syncs
 * player tab list data across servers within a sync group.
 *
 * @author Limo
 * @date 2/3/2026
 */
class RedisTabSync(
    val syncGroup: String
) : Terminable
{
    companion object
    {
        private const val CHANNEL_PREFIX = "tabsync"
    }

    private val terminable = CompositeTerminable.create()

    private val aware by lazy {
        AwareBuilder.of<AwareMessage>("$CHANNEL_PREFIX:$syncGroup")
            .logger(Lemon.instance.logger)
            .codec(AwareMessageCodec)
            .build()
    }

    private val syncedPlayers = ConcurrentHashMap<UUID, TabSyncPlayerData>()

    data class TabSyncPlayerData(
        val uuid: UUID,
        val username: String,
        val displayName: String,
        val serverId: String,
        val lastUpdate: Long = System.currentTimeMillis()
    )

    fun configure()
    {
        aware.listen("tab-sync-update") {
            val uuid = retrieve<String>("uuid").let { UUID.fromString(it) }
            val username = retrieve<String>("username")
            val displayName = retrieve<String>("displayName")
            val serverId = retrieve<String>("serverId")

            syncedPlayers[uuid] = TabSyncPlayerData(
                uuid = uuid,
                username = username,
                displayName = displayName,
                serverId = serverId
            )
        }

        aware.listen("tab-sync-remove") {
            val uuid = retrieve<String>("uuid").let { UUID.fromString(it) }
            syncedPlayers.remove(uuid)
        }

        aware.connect().toCompletableFuture().join()

        Schedulers.sync()
            .runRepeating({ _ ->
                for (player in Bukkit.getOnlinePlayers())
                {
                    val event = TabSyncSubscribeEvent(player, syncGroup)
                    Bukkit.getPluginManager().callEvent(event)

                    if (!event.isCancelled)
                    {
                        publishPlayerUpdate(player)
                    }
                }

                cleanupStaleEntries()
            }, 20L, 20L)
            .bindWith(terminable)

        Events.subscribe(org.bukkit.event.player.PlayerJoinEvent::class.java)
            .handler { event ->
                val tabEvent = TabSyncSubscribeEvent(event.player, syncGroup)
                Bukkit.getPluginManager().callEvent(tabEvent)

                if (!tabEvent.isCancelled)
                {
                    publishPlayerUpdate(event.player)
                }
            }
            .bindWith(terminable)

        Events.subscribe(org.bukkit.event.player.PlayerQuitEvent::class.java)
            .handler { event ->
                publishPlayerRemove(event.player)
            }
            .bindWith(terminable)
    }

    private fun publishPlayerUpdate(player: Player)
    {
        val lemonPlayer = PlayerHandler.find(player.uniqueId)
        val displayName = lemonPlayer?.getColoredName() ?: player.displayName

        AwareMessage.of(
            "tab-sync-update",
            aware,
            "uuid" to player.uniqueId.toString(),
            "username" to player.name,
            "displayName" to displayName,
            "serverId" to getServerId()
        ).publish()
    }

    private fun publishPlayerRemove(player: Player)
    {
        AwareMessage.of(
            "tab-sync-remove",
            aware,
            "uuid" to player.uniqueId.toString()
        ).publish()

        syncedPlayers.remove(player.uniqueId)
    }

    private fun cleanupStaleEntries()
    {
        val threshold = System.currentTimeMillis() - 30_000
        syncedPlayers.entries.removeIf { it.value.lastUpdate < threshold }
    }

    private fun getServerId(): String
    {
        return try
        {
            gg.scala.commons.agnostic.sync.ServerSync.local.id
        }
        catch (e: Exception)
        {
            "unknown"
        }
    }

    fun getSyncedPlayers(): Map<UUID, TabSyncPlayerData> = syncedPlayers.toMap()

    fun getPlayerCount(): Int = syncedPlayers.size

    override fun close()
    {
        for (player in Bukkit.getOnlinePlayers())
        {
            publishPlayerRemove(player)
        }

        terminable.close()
    }

    override fun closeAndReportException()
    {
        try
        {
            close()
        }
        catch (e: Exception)
        {
            Lemon.instance.logger.warning("Error closing RedisTabSync: ${e.message}")
        }
    }
}
