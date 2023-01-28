package gg.scala.lemon.handler

import com.mongodb.client.model.Filters
import gg.scala.lemon.menu.modmode.InspectionMenu
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
import org.bson.conversions.Bson
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object PlayerHandler
{
    val inventory = mutableMapOf<Int, ItemStack>()

    val players: ConcurrentHashMap<UUID, LemonPlayer>
        get() = DataStoreObjectControllerCache
            .findNotNull<LemonPlayer>()
            .localCache()

    fun find(uuid: UUID): LemonPlayer?
    {
        return players[uuid]
    }

    fun findPlayer(uuid: UUID): Optional<LemonPlayer>
    {
        return Optional.ofNullable(players[uuid])
    }

    fun findOnlinePlayer(name: String): LemonPlayer?
    {
        return players.values.firstOrNull {
            it.name.equals(name, true)
        }
    }

    fun findPlayer(name: String): Optional<LemonPlayer>
    {
        val player = Bukkit.getPlayer(name)

        if (player != null)
        {
            return Optional.ofNullable(
                players[player.uniqueId]
            )
        }

        return Optional.ofNullable(null)
    }

    fun findPlayer(player: Player?): Optional<LemonPlayer>
    {
        return Optional.ofNullable(players[player?.uniqueId])
    }

    private fun fetchPlayerAccountsFiltered(filter: Bson) =
        DataStoreObjectControllerCache
            .findNotNull<LemonPlayer>()
            .useLayerWithReturn<MongoDataStoreStorageLayer<LemonPlayer>, CompletableFuture<Map<UUID, LemonPlayer>>>(
                DataStoreStorageType.MONGO
            ) {
                this.loadAllWithFilter(filter)
            }

    fun fetchAlternateAccountsFor(uuid: UUID): CompletableFuture<List<LemonPlayer>>
    {
        return CompletableFuture
            .supplyAsync {
                findPlayer(uuid).orElse(null)
                    ?: AsyncLemonPlayer
                        .of(uuid)
                        .computeNow()
                        .join()
                        .firstOrNull()
            }
            .thenComposeAsync { lemonPlayer ->
                if (lemonPlayer == null)
                {
                    return@thenComposeAsync CompletableFuture
                        .completedFuture(
                            null to mapOf<UUID, LemonPlayer>()
                        )
                }

                fetchPlayerAccountsFiltered(
                    Filters.and(
                        Filters.ne("uniqueId", uuid.toString()),
                        Filters.or(
                            Filters.eq("ipAddress", lemonPlayer.ipAddress),
                            Filters.`in`(
                                "pastIpAddresses",
                                *lemonPlayer.pastIpAddresses.toTypedArray()
                            )
                        )
                    )
                ).thenApply { lemonPlayer to it }
            }
            .thenApplyAsync {
                it.first
                    ?: return@thenApplyAsync mutableListOf<LemonPlayer>()

                return@thenApplyAsync it.second.values.toList()
            }
            .exceptionally {
                it.printStackTrace()
                return@exceptionally listOf<LemonPlayer>()
            }
    }

    fun getCorrectedPlayerList(sender: CommandSender): Collection<LemonPlayer>
    {
        var currentList = Bukkit.getOnlinePlayers()
            .mapNotNull {
                findPlayer(it.uniqueId).orElse(null)
            }.sortedBy {
                -QuickAccess.realRank(it.bukkitPlayer!!).weight
            }

        if (currentList.size > 350)
        {
            currentList = currentList.subList(0, 350)
        }

        if (sender.isOp)
        {
            return currentList
        }

        return currentList.filter {
            !it.bukkitPlayer!!.hasMetadata("vanished") &&
                    !it.bukkitPlayer!!.hasMetadata("mod-mode")
        }
    }

}
