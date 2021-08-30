package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.util.CubedCacheUtil
import me.lucko.helper.Schedulers
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

object PlayerHandler {

    var players: MutableMap<UUID, LemonPlayer> = mutableMapOf()

    init {
        Schedulers.async().runRepeating(Runnable {
            players.values.stream().filter {
                !it.getPlayer().isPresent
            }.forEach {
                players.remove(it.uuid)?.save()
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

        val uuid = CubedCacheUtil.fetchUuid(name)

        return Optional.ofNullable(LemonPlayer(uuid!!, name, null))
    }

    fun findPlayer(player: Player): Optional<LemonPlayer> {
        return findPlayer(player.uniqueId)
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


}
