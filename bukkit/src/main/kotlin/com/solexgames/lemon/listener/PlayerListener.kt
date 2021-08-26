package com.solexgames.lemon.listener

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.LemonPlayer
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture

object PlayerListener : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val lemonPlayer = LemonPlayer(event.uniqueId, event.name, event.address.hostAddress)

        val completableFuture = CompletableFuture.supplyAsync {
            Lemon.instance.mongoHandler.playerCollection.find(
                Filters.eq(
                    "uuid",
                    event.uniqueId.toString()
                )
            ).first()
        }

        lemonPlayer.load(completableFuture)

        if (!lemonPlayer.loaded) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LemonConstants.PLAYER_DATA_LOAD
            )
            return
        }

        if (event.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_FULL && lemonPlayer.isStaff()) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.ALLOWED
        }

        Lemon.instance.playerHandler.players[event.uniqueId] = lemonPlayer
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        val serverHandler = Lemon.instance.serverHandler

        if (!lemonPlayer.isStaff()) {
            if (serverHandler.chatMuted || lemonPlayer.hasPermission("lemon.chat.bypass")) {
                event.isCancelled = true
                player.sendMessage("${CC.RED}Global chat is currently muted.")
            }

//            else if (player is muted) {
//                event.isCancelled = true
//                player.sendMessage("${CC.RED}You're muted for ...")
//            }
//            else if (serverHandler.slowChatTime != 0) {
//
//            }
        }

        // lemonPlayer chat cooldown check
    }

    private fun onDisconnect(player: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            Lemon.instance.playerHandler.players.remove(it.uniqueId)?.save()
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage = null
        onDisconnect(event.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerKick(event: PlayerKickEvent) {
        event.leaveMessage = null
        onDisconnect(event.player)
    }
}
