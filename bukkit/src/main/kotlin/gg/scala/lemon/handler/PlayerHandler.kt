package gg.scala.lemon.handler

import gg.scala.lemon.menu.modmode.InspectionMenu
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.Events
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

    fun fetchAlternateAccountsFor(uuid: UUID): CompletableFuture<List<LemonPlayer>>
    {
        return DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
            .loadAll(DataStoreStorageType.MONGO)
            .thenApplyAsync {
                val accounts = mutableListOf<LemonPlayer>()

                val lemonPlayer = findPlayer(uuid).orElse(null)
                    ?: AsyncLemonPlayer.of(uuid, true)
                        .future().join().firstOrNull()
                    ?: return@thenApplyAsync accounts

                for (entry in it)
                {
                    if (entry.value.uniqueId == uuid) continue

                    lemonPlayer.pastIpAddresses.keys.forEachIndexed { _, address ->
                        if (entry.value.pastIpAddresses.containsKey(address))
                        {
                            accounts.add(entry.value)
                            return@forEachIndexed
                        }
                    }
                }

                return@thenApplyAsync accounts
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
