package gg.scala.lemon.listener

import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.Listeners
import gg.scala.flavor.inject.Inject
import gg.scala.lemon.Lemon
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.cooldown.CooldownHandler
import gg.scala.lemon.cooldown.impl.CommandCooldown
import gg.scala.lemon.filter.ChatMessageFilterHandler
import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.logger.impl.`object`.CommandAsyncFileLogger
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess.sendChannelMessage
import gg.scala.store.controller.DataStoreObjectControllerCache
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.server.ServerCommandEvent

@Listeners
object PlayerListener : Listener
{
    @Inject
    lateinit var plugin: Lemon

    private val playerController by lazy {
        DataStoreObjectControllerCache
            .findNotNull<LemonPlayer>()
    }

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    fun onPlayerPreLoginHigh(
        event: AsyncPlayerPreLoginEvent
    )
    {
        if (plugin.settings.dummyServer)
        {
            this.playerController.localCache()[event.uniqueId] =
                LemonPlayer(
                    uniqueId = event.uniqueId,
                    name = event.name,
                    ipAddress = event.address.hostAddress ?: "",
                    firstLogin = true
                )
            return
        }

        val lemonPlayer = this.playerController
            .loadOptimalCopy(event.uniqueId) {
                LemonPlayer(
                    uniqueId = event.uniqueId,
                    name = event.name,
                    ipAddress = event.address.hostAddress ?: "",
                    firstLogin = true
                )
            }.join()

        // We're assuming the data object has
        // never been saved as the timestamp is 0L.
        if (lemonPlayer.timestamp == 0L)
        {
            lemonPlayer.handleIfFirstCreated()
            return
        }

        lemonPlayer.name = event.name

        lemonPlayer.ipAddress =
            event.address.hostAddress ?: ""

        lemonPlayer.handlePostLoad()
    }

    var defaultChannelProtection = { event: AsyncPlayerChatEvent -> }

    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true
    )
    fun onPlayerChat(event: AsyncPlayerChatEvent)
    {
        val player = event.player

        val lemonPlayer = PlayerHandler
            .findPlayer(player).orElse(null)

        lemonPlayer.declinePunishedAction {
            cancel(event, "${CC.RED}You cannot chat while you are $it")
        }

        if (event.isCancelled)
            return

        val mutePunishment = lemonPlayer
            .findApplicablePunishment(PunishmentCategory.MUTE)

        if (mutePunishment != null)
        {
            cancel(event, lemonPlayer.getPunishmentMessage(mutePunishment, false))
            return
        }

        if (event.message.contains("\${"))
        {
            cancel(event, "${CC.RED}You're not allowed to use this syntax.")
            return
        }

        val channelMatch = ChatChannelService
            .findAppropriateChannel(player, event.message)

        val composite = channelMatch.composite()

        if (composite.identifier() == "default")
        {
            if (lemonPlayer.getSetting("global-chat-disabled"))
            {
                cancel(event, "${CC.RED}You have chat disabled, please re-enable it to continue talking.")
                return
            }

            this.defaultChannelProtection.invoke(event)

            if (event.isCancelled)
            {
                return
            }
        }

        for (chatCheck in ChatHandler.chatChecks)
        {
            val result = chatCheck.invoke(event)

            if (result.second)
            {
                cancel(event, result.first)
                return
            }
        }

        if (event.isCancelled)
            return

        val preFormatted = channelMatch.preFormat(event.message)

        if (channelMatch.monitored)
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
                    val formatted = channelMatch
                        .composite().format(
                            player.uniqueId, player, preFormatted,
                            Lemon.instance.settings.id,
                            if (
                                !channelMatch.usesRealRank &&
                                (player.hasMetadata("disguised") || lemonPlayer.disguiseRank() != null)
                            )
                            {
                                lemonPlayer.disguiseRank() ?: RankHandler.getDefaultRank()
                            } else
                            {
                                lemonPlayer.activeGrant!!.getRank()
                            }
                        )

                    channelMatch.sendToPlayer(
                        player, formatted
                    )

                    event.isCancelled = true
                    return
                }
            }
        }

        if (event.isCancelled)
            return

        event.isCancelled = true

        if (channelMatch.distributed)
        {
            sendChannelMessage(
                channelMatch.composite().identifier(),
                preFormatted, lemonPlayer
            )
        } else
        {
            for (target in Bukkit.getOnlinePlayers())
            {
                if (
                    !channelMatch
                        .permissionLambda
                        .invoke(target)
                )
                    continue

                if (
                    !channelMatch
                        .displayToPlayer
                        .invoke(player, target)
                )
                    continue

                val lemonTarget = PlayerHandler
                    .find(target.uniqueId)

                if (lemonTarget != null)
                {
                    if (
                        channelMatch.composite().identifier() == "default" &&
                        !player.hasPermission("lemon.staff")
                    )
                    {
                        if (lemonTarget.getSetting("global-chat-disabled"))
                        {
                            continue
                        }
                    }

                    if (
                        lemonTarget.ignoring.contains(player.uniqueId) &&
                        !player.hasPermission("lemon.staff")
                    )
                    {
                        continue
                    }
                }

                channelMatch.sendToPlayer(
                    target, channelMatch
                        .composite().format(
                            player.uniqueId, target, preFormatted,
                            Lemon.instance.settings.id,
                            if (
                                !channelMatch.usesRealRank &&
                                (player.hasMetadata("disguised") || lemonPlayer.disguiseRank() != null)
                            )
                            {
                                lemonPlayer.disguiseRank() ?: RankHandler.getDefaultRank()
                            } else
                            {
                                lemonPlayer.activeGrant!!.getRank()
                            }
                        )
                )
            }

            if (plugin.settings.consoleChat)
            {
                ChatChannelService.audiences.console().sendMessage(
                    channelMatch
                        .composite().format(
                            player.uniqueId, player, preFormatted,
                            Lemon.instance.settings.id,
                            if (
                                !channelMatch.usesRealRank &&
                                (player.hasMetadata("disguised") || lemonPlayer.disguiseRank() != null)
                            )
                            {
                                lemonPlayer.disguiseRank() ?: RankHandler.getDefaultRank()
                            } else
                            {
                                lemonPlayer.activeGrant!!.getRank()
                            }
                        )
                )
            }
        }
    }

    @EventHandler(
        priority = EventPriority.HIGHEST
    )
    fun onPlayerJoin(event: PlayerJoinEvent)
    {
        val lemonPlayer = PlayerHandler
            .find(event.player.uniqueId)
            ?: return event.player.kickPlayer(
                plugin.languageConfig.playerDataLoad
            )

        event.player.removeMetadata("frozen", plugin)
        event.joinMessage = null

        updatePlayerRecord()

        lemonPlayer.performConnectionTasks()

        VisibilityHandler.updateToAll(event.player)
        NametagHandler.reloadPlayer(event.player)
    }

    private fun updatePlayerRecord()
    {
        val highestPlayerCount = ServerSync.getLocalGameServer()
            .getMetadataValue<Int?>(
                "lemon", "highest-player-count"
            )

        val currentPlayerCount = Bukkit.getOnlinePlayers().size

        if (highestPlayerCount == null || currentPlayerCount > highestPlayerCount.toInt())
        {
            ServerSync.getLocalGameServer().setMetadata(
                Key.key("lemon", "highest-player-count"), currentPlayerCount
            )
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

        if (!command.startsWith("/discord", true))
        {
            lemonPlayer.declinePunishedAction {
                cancel(event, "${CC.RED}You cannot perform commands while being $it")
            }
        }

        if (event.isCancelled)
            return

        if (command.contains(":") && !player.isOp)
        {
            cancel(event, "${CC.RED}You're not allowed to use this syntax.")
            return
        }

        if (!lemonPlayer.hasPermission("lemon.command-blacklist.bypass"))
        {
            plugin.settings.blacklistedCommands.forEach {
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

    @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
    )
    fun onPlayerQuit(event: PlayerQuitEvent)
    {
        event.quitMessage = null
        handleDisconnection(event.player)
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

    private fun cancel(
        event: Cancellable, player: Player, message: String
    )
    {
        player.sendMessage("${CC.RED}$message")
        event.isCancelled = true
    }

    private fun cancel(event: PlayerEvent, message: String)
    {
        event.player.sendMessage("${CC.RED}$message")

        if (event is Cancellable)
            event.isCancelled = true
    }

    private fun handleDisconnection(player: Player)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            PlayerHandler.players.remove(it.uniqueId)?.save()
        }
    }
}
