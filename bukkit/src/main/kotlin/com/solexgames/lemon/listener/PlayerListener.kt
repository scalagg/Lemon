package com.solexgames.lemon.listener

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.util.MapBuilder
import com.solexgames.lemon.util.other.Cooldown
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import java.util.concurrent.CompletableFuture

class PlayerListener : Listener {

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginHigh(event: AsyncPlayerPreLoginEvent) {
        if (!Lemon.canJoin) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Lemon.instance.languageConfig.serverNotLoaded)
            return
        }

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

        if (event.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_FULL && lemonPlayer.isStaff()) {
            event.loginResult = AsyncPlayerPreLoginEvent.Result.ALLOWED
        }

        Lemon.instance.playerHandler.players[event.uniqueId] = lemonPlayer
    }

    @EventHandler(
        priority = EventPriority.LOWEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginLow(event: AsyncPlayerPreLoginEvent) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(event.uniqueId).orElse(null)

        if (lemonPlayer == null) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Lemon.instance.languageConfig.playerDataLoad + " (${lemonPlayer})"
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        val chatHandler = Lemon.instance.chatHandler

        if (!lemonPlayer.isStaff()) {
            // TODO: 27/08/2021 add mute check
            if (chatHandler.chatMuted || lemonPlayer.hasPermission("lemon.chat.bypass")) {
                cancel(event, "${CC.RED}Global chat is currently muted.")
            } else if (chatHandler.slowChatTime != 0) {
                if (lemonPlayer.slowChatCooldown.isActive()) {
                    val formatted = TimeUtil.millisToSeconds((lemonPlayer.slowChatCooldown.getRemaining().toFloat()).toLong())

                    cancel(event, "${CC.RED}Global chat is currently slowed, please wait ${formatted}.")
                    return
                }

                lemonPlayer.slowChatCooldown = Cooldown(chatHandler.slowChatTime * 1000L)
            } else {
                if (lemonPlayer.chatCooldown.isActive()) {
                    val formatted = TimeUtil.millisToSeconds(lemonPlayer.chatCooldown.getRemaining())

                    cancel(event, "${CC.RED}You're on chat cooldown, please wait ${formatted}.")
                    return
                }

                lemonPlayer.resetChatCooldown()
            }
        }

        if (event.isCancelled)
            return

        if (!lemonPlayer.getSetting("global-chat")) {
            cancel(event, "${CC.RED}You have global chat disabled, re-enable it to continue chatting.")
            return
        }

        var channelMatch: Channel? = null

        chatHandler.channels.forEach { (_, channel) ->
            if (channel.isPrefixed(event.message)) {
                channelMatch = channel
                return@forEach
            }
        }

        if (channelMatch == null) {
            channelMatch = chatHandler.channels["default"]
        }

        val channelOverride = chatHandler.findChannelOverride(player)

        channelOverride.ifPresent {
            channelMatch = it
        }

        if (channelMatch == null) /* could happen if someone messes with channels externally*/ {
            cancel(event, "${CC.RED}Something's wrong with global chat, please contact a developer. (104)")
            return
        }

        if (channelMatch?.isGlobal() == true) {
            RedisHandler.buildMessage(
                "channel-message",
                MapBuilder<String, String>()
                    .put("channel", channelMatch!!.getId())
                    .put("message", event.message)
                    .put("sender", player.name)
                    .put("rank", lemonPlayer.activeGrant!!.getRank().uuid.toString())
                    .build()
            )
        } else {
            Bukkit.getOnlinePlayers().forEach {
                var canReceive = true

                if (channelMatch?.getPermission() != null) {
                    canReceive = it.hasPermission(channelMatch?.getPermission())
                }

                if (!canReceive) {
                    return@forEach
                }

                val lemonTarget = Lemon.instance.playerHandler.findPlayer(it).orElse(null)

                if (lemonTarget != null) {
                    if (lemonTarget.getSetting("global-chat-disabled")) {
                        return@forEach
                    }

                    if (lemonTarget.getSetting(channelMatch?.getId() + "-disabled")) {
                        return@forEach
                    }
                }

                if (!canReceive) {
                    return@forEach
                }

                player.sendMessage(channelMatch?.getFormatted(
                    event.message,
                    player.name,
                    lemonPlayer.activeGrant?.getRank() ?: Lemon.instance.rankHandler.getDefaultRank(),
                    it
                ))
            }
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(event.player)
        event.joinMessage = null

        lemonPlayer.orElse(null) ?: event.player.kickPlayer(Lemon.instance.languageConfig.playerDataLoad)
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

    private fun cancel(event: PlayerEvent, message: String) {
        event.player.sendMessage(message)
        (event as Cancellable).isCancelled = true
    }

    private fun onDisconnect(player: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            Lemon.instance.playerHandler.players.remove(it.uuid)?.save()
        }
    }
}
