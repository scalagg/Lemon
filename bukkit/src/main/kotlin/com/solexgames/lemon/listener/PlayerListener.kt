package com.solexgames.lemon.listener

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.LemonPlayer
import com.solexgames.lemon.player.channel.Channel
import com.solexgames.lemon.util.other.Cooldown
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import java.util.concurrent.CompletableFuture

object PlayerListener: Listener {

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginHigh(event: AsyncPlayerPreLoginEvent) {
        if (!Lemon.canJoin) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LemonConstants.SERVER_NOT_LOADED)
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

        if (lemonPlayer == null || !lemonPlayer.loaded) {
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER, LemonConstants.PLAYER_DATA_LOAD
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        val serverHandler = Lemon.instance.serverHandler
        val chatSlowed = Lemon.instance.serverHandler.slowChatTime != 0

        if (!lemonPlayer.isStaff()) {
            if (serverHandler.chatMuted || lemonPlayer.hasPermission("lemon.chat.bypass")) {
                event.isCancelled = true
                player.sendMessage("${CC.RED}Global chat is currently muted.")
            } else if (chatSlowed) {
                if (lemonPlayer.slowChatCooldown.isActive()) {
                    val formatted = TimeUtil.millisToSeconds(lemonPlayer.slowChatCooldown.getRemaining())
                    player.sendMessage("${CC.RED}Global chat is currently slowed, please wait ${formatted}.")
                    event.isCancelled = true
                    return
                }

                lemonPlayer.slowChatCooldown = Cooldown(serverHandler.slowChatTime * 1000L)
            } else {
                if (lemonPlayer.chatCooldown.isActive()) {
                    val formatted = TimeUtil.millisToSeconds(lemonPlayer.chatCooldown.getRemaining())
                    player.sendMessage("${CC.RED}You're on chat cooldown, please wait ${formatted}.")
                    event.isCancelled = true
                    return
                }

                lemonPlayer.resetChatCooldown()
            }
        }

        if (event.isCancelled)
            return

        val globalChatDisabledMeta = lemonPlayer.getMetadata("global-chat-disabled")
        val globalChatDisabled = globalChatDisabledMeta != null && globalChatDisabledMeta.asBoolean()

        if (globalChatDisabled) {
            player.sendMessage("${CC.RED}You've disabled global chat, please re-enable it to continue chatting.")
            event.isCancelled = true
            return
        }

        val chatHandler = Lemon.instance.chatHandler
        var channelMatch: Channel? = null

        chatHandler.channels.forEach { (_, u) ->
            if (u.isPrefixed(event.message)) {
                channelMatch = u
                return@forEach
            }
        }

        if (channelMatch == null) {
            channelMatch = chatHandler.channels["default"]
        }

        val channelOverride = chatHandler.channelOverride

        if (channelOverride != null) {
            channelMatch = channelOverride
        }

        if (channelMatch == null) /* could happen if someone messes with channels externally*/ {
            player.sendMessage("${CC.RED}Something's wrong with global chat, please contact a developer. (104)")
            event.isCancelled = true
            return
        }

        if (channelMatch?.isGlobal() == true) {
            // TODO: 8/27/2021 setup redis shit & send globally w/ data
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
                    val globalChatDisabledTargetMeta = lemonTarget.getMetadata("global-chat-disabled")
                    val globalChatDisabledTarget = globalChatDisabledTargetMeta != null && globalChatDisabledTargetMeta.asBoolean()

                    if (globalChatDisabledTarget) {
                        return@forEach
                    }

                    val channelDisabledTargetMeta = lemonTarget.getMetadata(channelMatch?.getId() + "-disabled")
                    val channelDisabledTarget = channelDisabledTargetMeta != null && channelDisabledTargetMeta.asBoolean()

                    if (channelDisabledTarget) {
                        return@forEach
                    }
                }

                if (!canReceive) {
                    return@forEach
                }

                player.sendMessage(lemonPlayer.activeGrant?.let {
                        it1 -> channelMatch?.getFormatted(event.message, player.name, it1.getRank(), it)
                })
            }
        }

        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(event.player)
        event.joinMessage = null

        // TODO: 8/27/2021 add more stuff

        lemonPlayer.orElse(null) ?: event.player.kickPlayer(LemonConstants.PLAYER_DATA_LOAD)
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

    private fun onDisconnect(player: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            Lemon.instance.playerHandler.players.remove(it.uniqueId)?.save()
        }
    }
}
