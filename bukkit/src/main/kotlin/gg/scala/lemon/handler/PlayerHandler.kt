package gg.scala.lemon.handler

import com.mongodb.client.model.Filters
import gg.scala.cache.uuid.ScalaStoreUuidCache
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.throwAnyExceptions
import gg.scala.lemon.util.QuickAccess
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.MongoDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import org.bson.conversions.Bson
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@Service
object PlayerHandler
{
    val inventory = mutableMapOf<Int, ItemStack>()

    val players: ConcurrentHashMap<UUID, LemonPlayer>
        get() = DataStoreObjectControllerCache
            .findNotNull<LemonPlayer>()
            .localCache()

    @Configure
    fun configure()
    {
        // We handle names through our UUID cache
        // programmatically, so we have to create
        // indexes for it manually
        DataStoreObjectControllerCache
            .findNotNull<LemonPlayer>()
            .mongo()
            .createIndexesFor("name")
    }

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

    fun updateAccountUsernamesWithUsername(uniqueId: UUID) =
        fetchPlayerAccountsFiltered(
            Filters.and(
                Filters.eq("name", ScalaStoreUuidCache.username(uniqueId)),
                Filters.ne("uniqueId", uniqueId.toString())
            )
        ).thenComposeAsync {
            if (it.isEmpty())
                return@thenComposeAsync CompletableFuture
                    .completedFuture(null)

            CompletableFuture.allOf(
                *it.values
                    .map { player ->
                        // new username is automatically grabbed
                        player.save()
                    }
                    .toTypedArray()
            )
        }!!

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
            .throwAnyExceptions(listOf())
    }

    fun getCorrectedPlayerList(sender: CommandSender): Collection<LemonPlayer>
    {
        val currentList = Bukkit.getOnlinePlayers()
            .mapNotNull {
                findPlayer(it.uniqueId).orElse(null)
            }.sortedBy {
                -QuickAccess.realRank(it.bukkitPlayer!!).weight
            }

        if (sender.isOp)
        {
            return currentList
        }

        return currentList.filter {
            !it.bukkitPlayer!!.hasMetadata("vanished")
        }
    }

}
