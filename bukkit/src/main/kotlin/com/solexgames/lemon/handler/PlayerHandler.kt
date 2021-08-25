package com.solexgames.lemon.handler

import com.solexgames.lemon.player.LemonPlayer
import net.evilblock.cubed.Cubed
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object PlayerHandler {

    var players: MutableMap<UUID, LemonPlayer> = mutableMapOf()

    init {
        Tasks.asyncTimer(20L * 60L, 20L * 60L) {
            players.values.stream().filter {
                !it.getPlayer().isPresent
            }.forEach {
                players.remove(it.uniqueId)?.save()
            }
        }
    }

    fun findPlayer(uuid: UUID): Optional<LemonPlayer> {
        if (players.containsKey(uuid)) {
            return Optional.ofNullable(players[uuid])
        }

        val offline = Bukkit.getOfflinePlayer(uuid)
        val name = Cubed.instance.uuidCache.name(uuid)

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

        val uuid = Cubed.instance.uuidCache.uuid(name)

        return Optional.ofNullable(LemonPlayer(uuid!!, name, null))
    }

    fun findPlayer(player: Player): Optional<LemonPlayer> {
        return findPlayer(player.uniqueId)
    }
}
