package com.solexgames.lemon.command.environment

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.command.CommandSender

@CommandAlias("environment")
@CommandPermission("lemon.command.environment")
class EnvironmentCommand : BaseCommand() {

    @Default
    @HelpCommand
    @Syntax("[page]")
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("fetch-online")
    @Syntax("<group>")
    @Description("Fetch online servers.")
    fun onFetchOnline(sender: CommandSender, group: String) {
        Lemon.instance.handler.fetchOnlineServerInstancesByGroup(group)?.whenComplete { t, u ->
            if (u != null || t == null) {
                sender.sendMessage("${CC.RED}No server in the group ${CC.YELLOW}$group${CC.RED} is online.")
                return@whenComplete
            }

            sender.sendMessage("${CC.PRI}${CC.BOLD}Online Servers in Group $group:")

            t.forEach {
                sender.sendMessage("${CC.GRAY} - ${CC.SEC}${it.serverId}")
            }
        }
    }

    @Subcommand("fetch-all")
    @Description("Fetch all servers.")
    fun onFetchAll(sender: CommandSender) {
        Lemon.instance.handler.fetchAllCachedServers()?.whenComplete { t, u ->
            if (u != null || t == null) {
                sender.sendMessage("${CC.RED}No servers found.")
                return@whenComplete
            }

            sender.sendMessage("${CC.PRI}${CC.BOLD}Cached Servers:")

            t.forEach {
                sender.sendMessage("${CC.GRAY} - ${CC.SEC}${it.value.serverId}")
            }
        }
    }

    @Syntax("<id>")
    @Subcommand("fetch")
    @Description("Fetch server by id.")
    fun onFetchServer(sender: CommandSender, id: String) {
        Lemon.instance.handler.fetchServerInstanceById(id)?.whenComplete { t, u ->
            if (u != null || t == null) {
                sender.sendMessage("${CC.RED}No server by the name ${CC.YELLOW}$id${CC.RED} is online.")
                return@whenComplete
            }

            sender.sendMessage("${CC.PRI}${CC.BOLD}${t.serverId} Information:")
            sender.sendMessage("${CC.GRAY}Group: ${CC.WHITE}${t.serverGroup}")
            sender.sendMessage("${CC.GRAY}Online: ${CC.WHITE}${t.onlinePlayers}")
            sender.sendMessage("${CC.GRAY}Max Players: ${CC.WHITE}${t.maxPlayers}")

            sender.sendMessage("${CC.GRAY}TPS: ${CC.WHITE}${String.format("%.2f",
                t.ticksPerSecond.coerceAtMost(20.0)
            )}")

            sender.sendMessage("${CC.GRAY}Whitelisted: ${CC.WHITE}${t.whitelisted}")
            sender.sendMessage("${CC.GRAY}Online Mode: ${CC.WHITE}${t.onlineMode}")
            sender.sendMessage("")
            sender.sendMessage("${CC.GRAY}Version: ${CC.WHITE}${t.version}")
            sender.sendMessage("${CC.GRAY}Last Heartbeat: ${CC.WHITE}${DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - t.lastHeartbeat, true, true)}")
        }
    }
}
