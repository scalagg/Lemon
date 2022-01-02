package gg.scala.lemon.listener

import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.cooldown.CooldownHandler
import gg.scala.lemon.cooldown.impl.ChatCooldown
import gg.scala.lemon.cooldown.impl.CommandCooldown
import gg.scala.lemon.cooldown.impl.SlowChatCooldown
import gg.scala.lemon.filter.ChatMessageFilterHandler
import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.handler.FrozenPlayerHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.logger.impl.`object`.ChatAsyncFileLogger
import gg.scala.lemon.logger.impl.`object`.CommandAsyncFileLogger
import gg.scala.lemon.menu.frozen.PlayerFrozenMenu
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.channel.Channel
import gg.scala.lemon.player.event.impl.PostFreezeEvent
import gg.scala.lemon.player.extension.PlayerCachingExtension
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.realRank
import gg.scala.lemon.util.QuickAccess.shouldBlock
import gg.scala.lemon.util.queueForDispatch
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
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
import org.bukkit.event.server.ServerCommandEvent

object PlayerListener : Listener
{
    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginHigh(event: AsyncPlayerPreLoginEvent)
    {
        if (!Lemon.canJoin)
        {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Lemon.instance.languageConfig.serverNotLoaded)
            return
        }

        var created = false

        DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
            .loadAndCache(event.uniqueId, {
                created = true
                return@loadAndCache LemonPlayer(
                    event.uniqueId, event.name,
                    event.address.hostAddress ?: ""
                )
            }, DataStoreStorageType.MONGO)
            .thenAccept {
                if (created)
                {
                    it.handleIfFirstCreated()
                } else
                {
                    it.name = event.name

                    if (!it.savePreviousIpAddressAsCurrent)
                    {
                        it.ipAddress = event.address.hostAddress ?: ""
                    }

                    it.handlePostLoad()
                }
            }
    }

    @EventHandler(
        priority = EventPriority.LOWEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginLow(event: AsyncPlayerPreLoginEvent)
    {
        val lemonPlayer = PlayerHandler.findPlayer(event.uniqueId).orElse(null)

        if (lemonPlayer != null)
        {
            if (event.loginResult == AsyncPlayerPreLoginEvent.Result.KICK_FULL)
            {
                event.loginResult = AsyncPlayerPreLoginEvent.Result.ALLOWED
            }
        }
    }

    @EventHandler
    fun onFreezeEvent(event: PostFreezeEvent)
    {
        PlayerFrozenMenu().openMenu(event.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onPlayerChat(event: AsyncPlayerChatEvent)
    {
        val player = event.player
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.hasPermission("lemon.2fa.forced") && !lemonPlayer.isAuthExempt() && !player.hasMetadata("authenticated"))
        {
            event.isCancelled = true
            event.player.sendMessage("${CC.RED}You must authenticate before chatting.")
            return
        }

        val ipRelativePunishment = lemonPlayer.fetchApplicablePunishmentInCategory(PunishmentCategory.IP_RELATIVE)

        if (ipRelativePunishment != null)
        {
            cancel(event, "${CC.RED}You cannot chat while being in relation to a banned player.")
            return
        }

        val mutePunishment = lemonPlayer.fetchApplicablePunishmentInCategory(PunishmentCategory.MUTE)

        if (mutePunishment != null)
        {
            cancel(event, lemonPlayer.getPunishmentMessage(mutePunishment))
            return
        }

        val blacklistPunishment = lemonPlayer.fetchApplicablePunishmentInCategory(PunishmentCategory.BLACKLIST)

        if (blacklistPunishment != null)
        {
            cancel(event, "${CC.RED}You cannot chat while being blacklisted.")
            return
        }

        val banPunishment = lemonPlayer.fetchApplicablePunishmentInCategory(PunishmentCategory.BAN)

        if (banPunishment != null)
        {
            cancel(event, "${CC.RED}You cannot chat while being banned.")
            return
        }

        var channelMatch: Channel? = null

        lemonPlayer.getMetadata("channel")?.let {
            val possibleChannel = ChatHandler.channels[it.asString()]

            if (possibleChannel != null)
            {
                channelMatch = possibleChannel
            }
        }

        val channelOverride = ChatHandler.findChannelOverride(player)

        channelOverride.ifPresent {
            channelMatch = it
        }

        ChatHandler.channels.forEach { (_, channel) ->
            if (channel.isPrefixed(event.message))
            {
                channelMatch = channel
                return@forEach
            }
        }

        if (channelMatch == null)
        {
            channelMatch = ChatHandler.channels["default"]
        }

        if (channelMatch == null)
        {
            cancel(event, "${CC.RED}We could not process your chat message")
            return
        }

        if (channelMatch!!.getId() == "default")
        {
            if (lemonPlayer.getSetting("global-chat-disabled"))
            {
                cancel(event, "${CC.RED}You have chat disabled, please re-enable it to continue talking.")
                return
            }

            if (ChatHandler.chatMuted && !lemonPlayer.hasPermission("lemon.mutechat.bypass"))
            {
                cancel(event, "${CC.RED}The chat is currently muted, please wait until it is no longer muted to talk.")
            } else if (ChatHandler.slowChatTime != 0 && !lemonPlayer.hasPermission("lemon.slowchat.bypass"))
            {
                val slowChat = CooldownHandler.find(
                    SlowChatCooldown::class.java
                )!!

                if (slowChat.isActive(player))
                {
                    val formatted = slowChat.getRemainingFormatted(player)

                    cancel(event, "${CC.RED}The chat is currently slowed, please wait $formatted before talking again.")
                    return
                }

                slowChat.addOrOverride(player)
            } else
            {
                if (!lemonPlayer.hasPermission("lemon.cooldown.chat.bypass"))
                {
                    val chat = CooldownHandler.find(
                        ChatCooldown::class.java
                    )!!

                    if (!CooldownHandler.notifyAndContinue(ChatCooldown::class.java, player))
                    {
                        event.isCancelled = true
                        return
                    }

                    chat.addOrOverride(player)
                }
            }
        }

        if (event.isCancelled)
            return

        if (channelMatch?.getId() == "default")
        {
            val result = ChatMessageFilterHandler
                .handleMessageFilter(
                    player, event.message,
                    !player.hasPermission("lemon.filter.bypass")
                )

            if (result)
            {
                if (player.hasPermission("lemon.filter.bypass"))
                {
                    player.sendMessage("${CC.RED}That message would've been filtered!")
                } else
                {
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
        }

        if (event.isCancelled)
            return

        event.isCancelled = true

        if (channelMatch?.isGlobal() == true)
        {
            RedisHandler.buildMessage(
                "channel-message",
                hashMapOf(
                    "channel" to channelMatch!!.getId(),
                    "message" to event.message,
                    "sender" to lemonPlayer.name,
                    "rank" to lemonPlayer.activeGrant!!
                        .getRank().uuid.toString(),
                    "server" to Lemon.instance.settings.id
                )
            ).queueForDispatch()
        } else
        {
            for (target in Bukkit.getOnlinePlayers())
            {
                val canReceive = channelMatch!!.hasPermission(target)

                if (!canReceive)
                {
                    continue
                }

                val lemonTarget = PlayerHandler.findPlayer(target).orElse(null)

                if (lemonTarget != null)
                {
                    if (channelMatch!!.getId() == "default" && !player.hasPermission("lemon.staff"))
                    {
                        if (lemonTarget.getSetting("global-chat-disabled"))
                        {
                            continue
                        }
                    }

                    if (lemonTarget.getSetting(channelMatch?.getId() + "-disabled"))
                    {
                        continue
                    }
                }

                if (!canReceive)
                {
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

            val selfMessage = channelMatch?.getFormatted(
                event.message, player.name,
                lemonPlayer.activeGrant!!.getRank(), player
            )!!

            if (Lemon.instance.settings.consoleChat)
            {
                Bukkit.getConsoleSender().sendMessage(selfMessage)
            }

            ChatAsyncFileLogger.queueForUpdates(selfMessage)
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent)
    {
        val lemonPlayer = PlayerHandler.findPlayer(event.player)

        if (!lemonPlayer.isPresent)
        {
            event.player.kickPlayer(Lemon.instance.languageConfig.playerDataLoad)
            return
        }

        event.joinMessage = null

        val highestPlayerCount = Lemon.instance.localInstance.metaData["highest-player-count"]
        val currentPlayerCount = Bukkit.getOnlinePlayers().size

        if (highestPlayerCount == null || currentPlayerCount > Integer.parseInt(highestPlayerCount))
        {
            Lemon.instance.localInstance.metaData["highest-player-count"] = currentPlayerCount.toString()
        }

        lemonPlayer.ifPresent { player ->
            player.handleOnConnection.forEach {
                it.invoke(event.player)
            }
            player.hasHandledOnConnection = true

            VisibilityHandler.updateToAll(event.player)
            NametagHandler.reloadPlayer(event.player)
        }
    }

    @EventHandler
    fun onEntityTarget(event: EntityTargetEvent)
    {
        if (event.reason == EntityTargetEvent.TargetReason.CUSTOM)
        {
            return
        }

        val entity = event.entity
        val target = event.target

        if (
            (entity is ExperienceOrb || entity is LivingEntity) && target is Player && target.hasMetadata("vanished")
        )
        {
            event.isCancelled = true
        }
    }

    @JvmStatic
    val BLACKLISTED_SYNTAX = "\\\$\\{*\\}".toRegex()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCommand(event: PlayerCommandPreprocessEvent)
    {
        val lemonPlayer = PlayerHandler.findPlayer(event.player).orElse(null) ?: return
        val player = event.player

        val commandCoolDown = CooldownHandler.find(
            CommandCooldown::class.java
        )!!

        if (!CooldownHandler.notifyAndContinue(CommandCooldown::class.java, player))
        {
            event.isCancelled = true
            return
        }

        val command = event.message.split(" ")[0].lowercase()

        if (event.message.matches(BLACKLISTED_SYNTAX))
        {
            cancel(event, "${CC.RED}You cannot use this syntax in commands!")
            return
        }

        if (!lemonPlayer.hasPermission("lemon.cooldown.command.bypass"))
        {
            commandCoolDown.addOrOverride(player)
        }

        if (!command.startsWith("/auth") && !command.startsWith("/2fa") && !command.startsWith("/setup") && shouldBlock(event.player))
        {
            cancel(event, "${CC.RED}You must authenticate before performing commands.")
            return
        }

        val ipRelativePunishment = lemonPlayer.fetchApplicablePunishmentInCategory(PunishmentCategory.IP_RELATIVE)

        if (ipRelativePunishment != null)
        {
            cancel(event, "${CC.RED}You cannot perform commands while being in relation to a punished player.")
            return
        }

        val blacklistPunishment = lemonPlayer.fetchApplicablePunishmentInCategory(PunishmentCategory.BLACKLIST)

        if (blacklistPunishment != null && command != "/register")
        {
            cancel(event, "${CC.RED}You cannot perform commands while being blacklisted.")
            return
        }

        val banPunishment = lemonPlayer.fetchApplicablePunishmentInCategory(PunishmentCategory.BAN)

        if (banPunishment != null && command != "/register")
        {
            cancel(event, "${CC.RED}You cannot perform commands while being banned.")
            return
        }

        if (command.contains(":") && !player.isOp)
        {
            cancel(event, "${CC.RED}This syntax is not accepted!")
            return
        }

        if (!lemonPlayer.hasPermission("lemon.command-blacklist.bypass"))
        {
            Lemon.instance.settings.blacklistedCommands.forEach {
                if (command == "/$it")
                {
                    cancel(event, "${CC.RED}You're not allowed to perform this command.")
                    return
                }
            }
        }

        CommandAsyncFileLogger.queueForUpdates(
            "${event.player.name}: ${event.message}"
        )
    }

    @EventHandler
    fun onConsoleCommand(event: ServerCommandEvent)
    {
        if (event.sender is ConsoleCommandSender)
        {
            CommandAsyncFileLogger.queueForUpdates(
                "Console: ${event.command}"
            )
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent)
    {
        event.quitMessage = null
        onDisconnect(event.player)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerKick(event: PlayerKickEvent)
    {
        event.leaveMessage = null
        onDisconnect(event.player)
    }

    @EventHandler
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent)
    {
        if (event.damager !is Player) return

        val player = event.damager as Player

        if (player.hasMetadata("vanished"))
        {
            player.sendMessage("${CC.RED}You cannot damage entities while in vanish.")
            event.isCancelled = true
        }
        if (player.hasMetadata("mod-mode"))
        {
            player.sendMessage("${CC.RED}You cannot damage entities while in mod-mode.")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent)
    {
        if (shouldBlock(event.player))
        {
            event.isCancelled = true
        }
    }


    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (event.player.hasMetadata("mod-mode"))
        {
            event.player.sendMessage("${CC.RED}You cannot break blocks while in mod-mode.")
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent)
    {
        if (event.player.hasMetadata("mod-mode"))
        {
            event.player.sendMessage("${CC.RED}You cannot place blocks while in mod-mode.")
            event.isCancelled = true
        }
    }

    private fun cancel(event: PlayerEvent, message: String)
    {
        event.player.sendMessage(message)
        (event as Cancellable).isCancelled = true
    }

    private fun onDisconnect(player: Player)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            val isFrozen = player.hasMetadata("frozen")

            if (isFrozen)
            {
                QuickAccess.sendStaffMessage(
                    null,
                    "${CC.AQUA}${coloredName(player)}${CC.D_AQUA} logged out while frozen.",
                    true,
                    QuickAccess.MessageType.NOTIFICATION
                )

                FrozenPlayerHandler.expirables.remove(player.uniqueId)
            }

            PlayerHandler.unModModePlayerSilent(player)
            PlayerCachingExtension.forget(it)

            PlayerHandler.players.remove(it.uniqueId)?.save()
        }
    }
}
