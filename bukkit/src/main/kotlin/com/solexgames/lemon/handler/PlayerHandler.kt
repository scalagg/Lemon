package com.solexgames.lemon.handler

import com.solexgames.lemon.player.LemonPlayer
import net.evilblock.cubed.Cubed
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.util.*
import java.util.concurrent.CompletableFuture

object PlayerHandler {

    var players: MutableMap<UUID, LemonPlayer> = mutableMapOf()

    fun getPlayer(uuid: UUID): Optional<LemonPlayer> {
        if (players.containsKey(uuid)) {
            return Optional.ofNullable(players[uuid])
        }

        val offline = Bukkit.getOfflinePlayer(uuid)
        val name = Cubed.instance.uuidCache.name(uuid)

        return Optional.ofNullable(if (offline.hasPlayedBefore()) { LemonPlayer(uuid, offline.name, null) } else { LemonPlayer(uuid, name, null) } )
    }

    fun getPlayer(name: String): Optional<LemonPlayer> {
        val player = Bukkit.getPlayer(name)

        if (player != null) {
            return Optional.ofNullable(players.getOrDefault(player.uniqueId, LemonPlayer(player.uniqueId, player.name, null)))
        }

        val offline = Bukkit.getOfflinePlayer(name)

        if (offline.hasPlayedBefore()) {
            return Optional.ofNullable(LemonPlayer(offline.uniqueId, offline.name, null))
        }

        val uuid = Cubed.instance.uuidCache.uuid(name)

        return Optional.ofNullable(LemonPlayer(uuid!!, name, null))
    }

    fun getPlayer(player: Player): Optional<LemonPlayer> {
        return getPlayer(player.uniqueId)
    }
}
