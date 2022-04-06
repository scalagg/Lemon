package gg.scala.lemon.command.environment

import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.handler.ServerHandler
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.command.CommandSender

@CommandAlias("environment|env")
@CommandPermission("lemon.command.environment")
object EnvironmentCommand : BaseCommand() {

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("fetch-online")
    @Description("Fetch online servers.")
    fun onFetchOnline(sender: CommandSender, group: String) {
        ServerHandler.fetchOnlineServerInstancesByGroup(group).whenComplete { t, u ->
            if (u != null || t == null) {
                sender.sendMessage("${CC.RED}No server in the group ${CC.YELLOW}$group${CC.RED} is online.")
                return@whenComplete
            }

            sender.sendMessage("${CC.PRI}${CC.BOLD}Online Servers in Group $group:")

            t.forEach {
                sender.sendMessage("${CC.GRAY} - ${CC.SEC}${it.value.serverId}")
            }
        }
    }

    @Syntax("<group> [boolean]")
    @Subcommand("whitelist-all-group")
    @Description("Whitelist/un-whitelist all servers in a particular group.")
    fun onFetchAll(sender: CommandSender, @Single group: String, boolean: Boolean) {
        sender.sendMessage("${CC.SEC}You've set the whitelist to ${CC.WHITE}$boolean${CC.SEC} on all servers in the group ${CC.PRI}$group${CC.SEC}.")

        RedisHandler.buildMessage(
            "mass-whitelist",
            "group" to group,
            "setting" to boolean.toString(),
            "issuer" to QuickAccess.nameOrConsole(sender)
        ).publish()
    }

    @Subcommand("fetch")
    @Description("Fetch server by id.")
    fun onFetchServer(sender: CommandSender, id: String) {
        ServerHandler.fetchServerInstanceById(id).whenComplete { t, u ->
            if (u != null || t == null) {
                sender.sendMessage("${CC.RED}No server by the name ${CC.YELLOW}$id${CC.RED} is online.")
                return@whenComplete
            }

            sender.sendMessage("${CC.PRI}${CC.BOLD}${t.serverId} Information:")
            sender.sendMessage("${CC.GRAY}Group: ${CC.WHITE}${t.serverGroup}")

            sender.sendMessage("")
            sender.sendMessage("${CC.GRAY}Players (Online): ${CC.WHITE}${t.onlinePlayers}")
            sender.sendMessage("${CC.GRAY}Players (Cap): ${CC.WHITE}${t.metaData["highest-player-count"]}")
            sender.sendMessage("${CC.GRAY}Players (Max): ${CC.WHITE}${t.maxPlayers}")
            sender.sendMessage("")

            sender.sendMessage("${CC.GRAY}TPS: ${CC.WHITE}${String.format("%.2f",
                t.ticksPerSecond.coerceAtMost(20.0)
            )}")

            sender.sendMessage("${CC.GRAY}Whitelisted: ${CC.WHITE}${t.whitelisted}")
            sender.sendMessage("${CC.GRAY}Online Mode: ${CC.WHITE}${t.onlineMode}")
            sender.sendMessage("")
            sender.sendMessage("${CC.GRAY}Version: ${CC.WHITE}${t.version}")
            sender.sendMessage("${CC.GRAY}Last Heartbeat: ${CC.WHITE}${DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - t.lastHeartbeat, true, false)}")
            sender.sendMessage("${CC.GRAY}Uptime: ${CC.WHITE}${DurationFormatUtils.formatDurationWords(System.currentTimeMillis() - (t.metaData["init"]?.toLong() ?: 0L), true, false)}")
        }
    }
}
