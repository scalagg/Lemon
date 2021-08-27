package com.solexgames.lemon.handler

import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.util.CubedCacheUtil
import me.lucko.helper.Schedulers
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object PlayerHandler {

    var players: MutableMap<UUID, LemonPlayer> = mutableMapOf()

    init {
        Schedulers.async().runRepeating(Runnable {
            players.values.stream().filter {
                !it.getPlayer().isPresent
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
        val name = CubedCacheUtil.fetchNameByUuid(uuid)

        return Optional.ofNullable(
            if (offline.hasPlayedBefore()) {
                LemonPlayer(uuid, offline.name, null)
            } else {
                LemonPlayer(uuid, name, null)
            }
        )
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

        if (offline.hasPlayedBefore()) {
            return Optional.ofNullable(LemonPlayer(offline.uniqueId, offline.name, null))
        }

        val uuid = CubedCacheUtil.fetchUuidByName(name)

        return Optional.ofNullable(LemonPlayer(uuid!!, name, null))
    }

    fun findPlayer(player: Player): Optional<LemonPlayer> {
        return findPlayer(player.uniqueId)
    }
}
