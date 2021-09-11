package com.solexgames.lemon.listener

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.quickaccess.remaining
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*

@ExperimentalStdlibApi
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

        Lemon.instance.mongoHandler.lemonPlayerLayer.fetchEntryByKey(event.uniqueId.toString())
            .whenComplete { lemonPlayer, throwable ->
                val lemonPlayerFinal: LemonPlayer?

                if (lemonPlayer == null || throwable != null) {
                    lemonPlayerFinal = LemonPlayer(event.uniqueId, event.name, event.address.hostAddress)
                    lemonPlayerFinal.handleIfFirstCreated()

                    throwable?.printStackTrace()
                } else {
                    lemonPlayer.ipAddress = event.address.hostAddress
                    lemonPlayer.handlePostLoad()

                    lemonPlayerFinal = lemonPlayer
                }

                Lemon.instance.playerHandler.players[event.uniqueId] = lemonPlayerFinal
            }
    }

    @EventHandler(
        priority = EventPriority.LOWEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginLow(event: AsyncPlayerPreLoginEvent) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(event.uniqueId).orElse(null)

        if (lemonPlayer == null) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Lemon.instance.languageConfig.playerDataLoad
            )
        } else {
            if (event.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_FULL && lemonPlayer.isStaff) {
                event.loginResult = AsyncPlayerPreLoginEvent.Result.ALLOWED
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        val chatHandler = Lemon.instance.chatHandler

        if (lemonPlayer.activeGrant == null) {
            cancel(event, "${CC.RED}Your profile has not loaded correctly.")
            cancel(event, "${CC.RED}Reconnect to resolve this issue.")
            return
        }

        if (chatHandler.chatMuted && !lemonPlayer.hasPermission("lemon.chat.mute.bypass")) {
            cancel(event, "${CC.RED}Global chat is currently muted.")
        } else if (chatHandler.slowChatTime != 0 && !lemonPlayer.hasPermission("lemon.chat.slow.bypass")) {
            if (lemonPlayer.cooldowns["slowChat"]?.isActive() == true) {
                val formatted = lemonPlayer.cooldowns["slowChat"]?.let { remaining(it) }

                cancel(event, "${CC.RED}Global chat is currently slowed, please wait ${formatted}.")
                return
            }

            lemonPlayer.cooldowns["slowChat"] = Cooldown(chatHandler.slowChatTime * 1000L)
        } else {
            if (!lemonPlayer.hasPermission("lemon.chat.delay.bypass")) {
                if (lemonPlayer.cooldowns["chat"]?.isActive() == true) {
                    val formatted = lemonPlayer.cooldowns["chat"]?.let { remaining(it) }

                    cancel(event, "${CC.RED}You're on chat cooldown, please wait ${formatted}.")
                    return
                }

                lemonPlayer.resetChatCooldown()
            }
        }

        if (event.isCancelled)
            return

        if (lemonPlayer.getSetting("global-chat-disabled")) {
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

        lemonPlayer.getMetadata("channel")?.let {
            val possibleChannel = chatHandler.channels[it.asString()]

            if (possibleChannel != null) {
                channelMatch = possibleChannel
            }
        }

        val channelOverride = chatHandler.findChannelOverride(player)

        channelOverride.ifPresent {
            channelMatch = it
        }

        if (channelMatch == null) {
            cancel(event, "${CC.RED}Something's wrong with global chat, please contact a developer. (104)")
            return
        }

        event.isCancelled = true

        if (channelMatch?.isGlobal() == true) {
            RedisHandler.buildMessage(
                "channel-message",
                buildMap {
                    put("channel", channelMatch!!.getId())
                    put("message", event.message)
                    put("sender", player.name)
                    put("rank", lemonPlayer.activeGrant!!.getRank().uuid.toString())
                }
            ).publishAsync()
        } else {
            for (target in Bukkit.getOnlinePlayers()) {
                var canReceive = true

                if (channelMatch!!.getPermission() != null) {
                    canReceive = target.hasPermission(channelMatch!!.getPermission())
                }

                if (!canReceive) {
                    continue
                }

                val lemonTarget = Lemon.instance.playerHandler.findPlayer(target).orElse(null)

                if (lemonTarget != null) {
                    if (lemonTarget.getSetting("global-chat-disabled")) {
                        continue
                    }

                    if (lemonTarget.getSetting(channelMatch?.getId() + "-disabled")) {
                        continue
                    }
                }

                if (!canReceive) {
                    continue
                }

                target.sendMessage(
                    channelMatch?.getFormatted(
                        event.message,
                        player.name,
                        lemonPlayer.activeGrant!!.getRank(),
                        target
                    )
                )
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(event.player)
        event.joinMessage = null

        lemonPlayer.orElse(null) ?: event.player.kickPlayer(Lemon.instance.languageConfig.playerDataLoad)

        val highestPlayerCount = Lemon.instance.getLocalServerInstance().metaData["highest-player-count"]
        val currentPlayerCount = Bukkit.getOnlinePlayers().size

        if (highestPlayerCount == null) {
            Lemon.instance.getLocalServerInstance().metaData["highest-player-count"] = currentPlayerCount.toString()
        } else {
            if (currentPlayerCount > Integer.parseInt(highestPlayerCount)) {
                Lemon.instance.getLocalServerInstance().metaData["highest-player-count"] = currentPlayerCount.toString()
            }
        }

        lemonPlayer.ifPresent { player ->
            player.handleOnConnection.forEach {
                it.invoke(event.player)
            }
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

    private fun cancel(event: PlayerEvent, message: String) {
        event.player.sendMessage(message)
        (event as Cancellable).isCancelled = true
    }

    private fun onDisconnect(player: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            Lemon.instance.playerHandler.players.remove(it.uniqueId)?.save()
        }
    }
}
