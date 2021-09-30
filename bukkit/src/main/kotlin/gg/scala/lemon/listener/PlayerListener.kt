package gg.scala.lemon.listener

import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.*
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.realRank
import gg.scala.lemon.util.QuickAccess.remaining
import gg.scala.lemon.util.QuickAccess.shouldBlock
import gg.scala.lemon.util.dispatchToLemon
import gg.scala.lemon.util.other.Cooldown
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetEvent
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

        DataStoreHandler.lemonPlayerLayer.fetchEntryByKey(event.uniqueId.toString())
            .whenComplete { lemonPlayer, throwable ->
                try {
                    val lemonPlayerFinal: LemonPlayer?
                    throwable?.printStackTrace()

                    if (lemonPlayer == null || throwable != null) {
                        lemonPlayerFinal = LemonPlayer(event.uniqueId, event.name, event.address.hostAddress)
                        lemonPlayerFinal.handleIfFirstCreated()
                    } else {
                        lemonPlayer.ipAddress = event.address.hostAddress
                        lemonPlayer.handlePostLoad()

                        lemonPlayerFinal = lemonPlayer
                    }

                    PlayerHandler.players[event.uniqueId] = lemonPlayerFinal
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    @EventHandler(
        priority = EventPriority.LOWEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginLow(event: AsyncPlayerPreLoginEvent) {
        val lemonPlayer = PlayerHandler.findPlayer(event.uniqueId).orElse(null)

        if (lemonPlayer == null) {
//            event.disallow(
//                AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Lemon.instance.languageConfig.playerDataLoad
//            )
        } else {
            if (event.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_FULL && lemonPlayer.isStaff) {
                event.loginResult = AsyncPlayerPreLoginEvent.Result.ALLOWED
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (player.hasMetadata("frozen")) {
            event.isCancelled = true

            Bukkit.getOnlinePlayers()
                .mapNotNull { PlayerHandler.findPlayer(player).orElse(null) }
                .filter { it.hasPermission("lemon.frozen.messages") && !it.hasMetadata("frozen-messages-disabled") }
                .forEach {
                    it.bukkitPlayer?.sendMessage("${CC.D_RED}[Frozen] ${coloredName(player)}${CC.GRAY}: ${CC.WHITE}${event.message}")
                }

            player.sendMessage("${CC.RED}Your message has been sent to our staff.")
            return
        }

        if (lemonPlayer.hasPermission("lemon.2fa.forced") && !lemonPlayer.isAuthExempt() && !player.hasMetadata("authenticated")) {
            event.isCancelled = true
            event.player.sendMessage("${CC.RED}You must authenticate before chatting.")
            return
        }

        val ipRelativePunishment = lemonPlayer.fetchPunishmentOf(PunishmentCategory.IP_RELATIVE)

        if (ipRelativePunishment != null) {
            cancel(event, "${CC.RED}You're not allowed to chat while being ip-punished.")
            return
        }

        val mutePunishment = lemonPlayer.fetchPunishmentOf(PunishmentCategory.MUTE)

        if (mutePunishment != null) {
            cancel(event, lemonPlayer.getPunishmentMessage(mutePunishment))
            return
        }

        val blacklistPunishment = lemonPlayer.fetchPunishmentOf(PunishmentCategory.BLACKLIST)

        if (blacklistPunishment != null) {
            cancel(event, "${CC.RED}You cannot chat while you're blacklisted.")
            return
        }

        val banPunishment = lemonPlayer.fetchPunishmentOf(PunishmentCategory.BAN)

        if (banPunishment != null) {
            cancel(event, "${CC.RED}You cannot chat while you're banned.")
            return
        }

        if (ChatHandler.chatMuted && !lemonPlayer.hasPermission("lemon.mutechat.bypass")) {
            cancel(event, "${CC.RED}Global chat is currently muted.")
        } else if (ChatHandler.slowChatTime != 0 && !lemonPlayer.hasPermission("lemon.slowchat.bypass")) {
            if (lemonPlayer.cooldowns["slowChat"]?.isActive() == true) {
                val formatted = lemonPlayer.cooldowns["slowChat"]?.let { remaining(it) }

                cancel(event, "${CC.RED}Global chat is currently slowed, please wait $formatted seconds.")
                return
            }

            lemonPlayer.cooldowns["slowChat"] = Cooldown(ChatHandler.slowChatTime * 1000L)
        } else {
            if (!lemonPlayer.hasPermission("lemon.cooldown.chat.bypass")) {
                if (lemonPlayer.cooldowns["chat"]?.isActive() == true) {
                    val formatted = lemonPlayer.cooldowns["chat"]?.let { remaining(it) }

                    cancel(event, "${CC.RED}You're on chat cooldown, please wait $formatted seconds.")
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

        ChatHandler.channels.forEach { (_, channel) ->
            if (channel.isPrefixed(event.message)) {
                channelMatch = channel
                return@forEach
            }
        }

        if (channelMatch == null) {
            channelMatch = ChatHandler.channels["default"]
        }

        lemonPlayer.getMetadata("channel")?.let {
            val possibleChannel = ChatHandler.channels[it.asString()]

            if (possibleChannel != null) {
                channelMatch = possibleChannel
            }
        }

        val channelOverride = ChatHandler.findChannelOverride(player)

        channelOverride.ifPresent {
            channelMatch = it
        }

        if (channelMatch == null) {
            cancel(event, "${CC.RED}Something's wrong with global chat, please contact a developer. (104)")
            return
        }

        if (channelMatch!!.getId() == "default") {
            if (FilterHandler.checkIfMessageFiltered(event.message, player)) {
                // they'll think the message sent ;O
                player.sendMessage(
                    channelMatch?.getFormatted(
                        event.message, player.name,
                        lemonPlayer.activeGrant!!.getRank(), player
                    )
                )

                event.isCancelled = true
                return
            }
        }

        event.isCancelled = true

        if (channelMatch?.isGlobal() == true) {
            RedisHandler.buildMessage(
                "channel-message",
                buildMap {
                    put("channel", channelMatch!!.getId())
                    put("message", event.message)
                    put("sender", lemonPlayer.name)
                    put("rank", lemonPlayer.activeGrant!!.getRank().uuid.toString())
                    put("server", Lemon.instance.settings.id)
                }
            ).dispatchToLemon()
        } else {
            for (target in Bukkit.getOnlinePlayers()) {
                var canReceive = true

                if (channelMatch!!.getPermission() != null) {
                    canReceive = target.hasPermission(channelMatch!!.getPermission())
                }

                if (!canReceive) {
                    continue
                }

                val lemonTarget = PlayerHandler.findPlayer(target).orElse(null)

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
                        realRank(player),
                        target
                    )
                )
            }

            // as Bukkit#getOnlinePlayers does not have console in it
            if (Lemon.instance.settings.consoleChat) {
                Bukkit.getConsoleSender().sendMessage(
                    channelMatch?.getFormatted(
                        event.message, player.name,
                        lemonPlayer.activeGrant!!.getRank(), player
                    )
                )
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val lemonPlayer = PlayerHandler.findPlayer(event.player)
        event.joinMessage = null

        lemonPlayer.orElse(null) ?: event.player.kickPlayer(Lemon.instance.languageConfig.playerDataLoad)

        val highestPlayerCount = Lemon.instance.getLocalServerInstance().metaData["highest-player-count"]
        val currentPlayerCount = Bukkit.getOnlinePlayers().size

        if (highestPlayerCount == null || currentPlayerCount > Integer.parseInt(highestPlayerCount)) {
            Lemon.instance.getLocalServerInstance().metaData["highest-player-count"] = currentPlayerCount.toString()
        }

        lemonPlayer.ifPresent { player ->
            player.handleOnConnection.forEach {
                it.invoke(event.player)
            }

            VisibilityHandler.updateToAll(event.player)
            NametagHandler.reloadPlayer(event.player)
        }
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onInteract(event: PlayerInteractEvent) {
        if (shouldBlock(event.player)) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent) {
        if (event.reason == EntityTargetEvent.TargetReason.CUSTOM) {
            return
        }

        val entity = event.entity
        val target = event.target

        if (
            (entity is ExperienceOrb || entity is LivingEntity) && target is Player && target.hasMetadata("vanished")
        ) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        val lemonPlayer = PlayerHandler.findPlayer(event.player).orElse(null) ?: return

        if (lemonPlayer.cooldowns["command"]?.isActive() == true) {
            val formatted = lemonPlayer.cooldowns["command"]?.let { remaining(it) }

            cancel(event, "${CC.RED}You're on command cooldown, please wait $formatted seconds.")
            return
        }

        val command = event.message.split(" ")[0]

        if (!command.startsWith("/auth", true) && !command.startsWith("/2fa", true) && !command.startsWith("/setup", true) && shouldBlock(event.player)) {
            cancel(event, "${CC.RED}You must authenticate before performing commands.")
            return
        }

        val ipRelativePunishment = lemonPlayer.fetchPunishmentOf(PunishmentCategory.IP_RELATIVE)

        if (ipRelativePunishment != null) {
            cancel(event, "${CC.RED}You're not allowed to perform commands while being ip-punished.")
            return
        }

        val blacklistPunishment = lemonPlayer.fetchPunishmentOf(PunishmentCategory.BLACKLIST)

        if (blacklistPunishment != null && command != "/register") {
            cancel(event, """
                ${CC.RED}You cannot perform commands while being blacklisted.
                ${CC.RED}You're only able to perform ${CC.YELLOW}/register${CC.RED}.
            """.trimIndent())
            return
        }

        val banPunishment = lemonPlayer.fetchPunishmentOf(PunishmentCategory.BAN)

        if (banPunishment != null && command != "/register") {
            cancel(event, """
                ${CC.RED}You cannot perform commands while being banned.
                ${CC.RED}You're only able to perform ${CC.YELLOW}/register${CC.RED}.
            """.trimIndent())
            return
        }

        if (command.contains(":") && !lemonPlayer.hasPermission("lemon.dev")) {
            cancel(event, "${CC.RED}You're not allowed to use this syntax.")
            return
        }

        if (!lemonPlayer.hasPermission("lemon.command-blacklist.bypass")) {
            Lemon.instance.settings.blacklistedCommands.forEach {
                if (command.equals(it, true)) {
                    cancel(event, "${CC.RED}You do not have permission to perform this command!")
                    return
                }
            }
        }

        if (!lemonPlayer.hasPermission("lemon.cooldown.command.bypass")) {
            lemonPlayer.cooldowns["command"] = Cooldown(1000L)
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

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.damager !is Player) return

        val player = event.damager as Player

        if (player.hasMetadata("vanished")) {
            player.sendMessage("${CC.RED}You may not damage entities while in vanish.")
        }
        if (player.hasMetadata("mod-mode")) {
            player.sendMessage("${CC.RED}You may not damage entities while in mod-mode.")
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (shouldBlock(event.player)) {
            event.isCancelled = true
            return
        }

        if (event.player.hasMetadata("mod-mode")) {
            event.player.sendMessage("${CC.RED}You may not break blocks while in mod-mode.")
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (shouldBlock(event.player)) {
            event.isCancelled = true
            return
        }

        if (event.player.hasMetadata("mod-mode")) {
            event.player.sendMessage("${CC.RED}You may not place blocks while in mod-mode.")
        }
    }

    private fun cancel(event: PlayerEvent, message: String) {
        event.player.sendMessage(message)
        (event as Cancellable).isCancelled = true
    }

    private fun onDisconnect(player: Player) {
        val lemonPlayer = PlayerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            val isFrozen = player.hasMetadata("frozen")

            if (isFrozen) {
                QuickAccess.sendStaffMessage(
                    null,
                    "${CC.AQUA}${coloredName(player)}${CC.D_AQUA} logged out while frozen.",
                    true,
                    QuickAccess.MessageType.NOTIFICATION
                )

                FrozenPlayerHandler.expirables.remove(player.uniqueId)
            }

            PlayerHandler.players.remove(it.uniqueId)?.save()
        }
    }
}
