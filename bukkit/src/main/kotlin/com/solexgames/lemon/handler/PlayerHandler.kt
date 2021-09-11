package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.util.CubedCacheUtil
import com.solexgames.lemon.util.quickaccess.uuid
import me.lucko.helper.Schedulers
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.HashMap

object PlayerHandler {

    var players: HashMap<UUID, LemonPlayer> = hashMapOf()

    init {
        Schedulers.async().runRepeating(Runnable {
            players.values.stream().filter {
                !it.bukkitPlayer.isPresent
            }.forEach {
                players.remove(it.uniqueId)?.save()
            }
        }, 20L * 60L, 20L * 60L)
    }

    fun findPlayer(uuid: UUID): Optional<LemonPlayer> {
        if (players.containsKey(uuid)) {
            return Optional.ofNullable(players[uuid])
        }

        val offline = Bukkit.getOfflinePlayer(uuid)
        val name = CubedCacheUtil.fetchName(uuid)

        return Optional.ofNullable(
            if (offline.hasPlayedBefore()) {
                LemonPlayer(uuid, offline.name, null)
            } else {
                LemonPlayer(uuid, name!!, null)
            }
        )
    }

    fun findOnlinePlayer(name: String): LemonPlayer? {
        return players.values.firstOrNull {
            it.name.equals(name, true)
        }
    }

    fun findPlayer(name: String): Optional<LemonPlayer> {
        val player = Bukkit.getPlayer(name)

        if (player != null) {
            return Optional.ofNullable(
                players.getOrDefault(
                    player.uniqueId,
                    LemonPlayer(player.uniqueId, player.name, null)
                )
            )
        }

        val offline = Bukkit.getOfflinePlayer(name)

        if (offline != null && offline.hasPlayedBefore()) {
            return Optional.ofNullable(LemonPlayer(offline.uniqueId, offline.name, null))
        }

        val uuid = CubedCacheUtil.fetchUuid(name)

        return Optional.ofNullable(LemonPlayer(uuid!!, name, null))
    }

    fun findPlayer(player: Player): Optional<LemonPlayer> {
        return Optional.ofNullable(players[player.uniqueId])
    }

    fun vanishPlayer(player: Player, power: Int = 0) {
        player.setMetadata("vanished", FixedMetadataValue(Lemon.instance, true))
        player.setMetadata("vanish-power", FixedMetadataValue(Lemon.instance, power))

        VisibilityHandler.updateToAll(player)
        NametagHandler.reloadPlayer(player)
    }

    fun unvanishPlayer(player: Player) {
        player.removeMetadata("vanished", Lemon.instance)
        player.removeMetadata("vanish-power", Lemon.instance)

        VisibilityHandler.updateToAll(player)
        NametagHandler.reloadPlayer(player)
    }

    fun fetchAlternateAccountsFor(lemonPlayer: LemonPlayer): CompletableFuture<Map<UUID, String>> {
        return Lemon.instance.mongoHandler.lemonPlayerLayer.fetchAllEntries().thenApply {
            val accounts = mutableMapOf<UUID, String>()

            it.forEach { entry ->
                lemonPlayer.pastIpAddresses.keys.forEachIndexed { _, address ->
                    if (entry.value.pastIpAddresses.containsKey(address)) {
                        accounts[entry.value.uniqueId] = address
                        return@forEachIndexed
                    }
                }
            }

            return@thenApply accounts
        }
    }

    fun getCorrectedPlayerList(sender: CommandSender): Collection<LemonPlayer> {
        var currentList = ArrayList(Bukkit.getOnlinePlayers())
            .mapNotNull {
                Lemon.instance.playerHandler.findPlayer(it.uniqueId).orElse(null)
            }.sortedBy { -it.activeGrant!!.getRank().weight }

        if (currentList.size > 350) {
            currentList = currentList.subList(0, 350) as ArrayList<LemonPlayer>
        }

        if (sender.hasPermission("lemon.staff")) {
            return currentList
        }

        return currentList.filter { !it.hasMetadata("vanished") && !it.hasMetadata("disguised") }
    }

}
