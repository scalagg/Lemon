package gg.scala.lemon.command.environment

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

@AutoRegister
@CommandAlias("environment|env")
@CommandPermission("lemon.command.environment")
object EnvironmentCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @AssignPermission
    @Subcommand("whitelist-scoped")
    @Description("Whitelist all servers in a group.")
    fun onFetchAll(sender: CommandSender, @Single group: String, boolean: Boolean)
    {
        sender.sendMessage("${CC.SEC}You've set the whitelist to ${CC.WHITE}$boolean${CC.SEC} on all servers in the group ${CC.PRI}$group${CC.SEC}.")

        RedisHandler.buildMessage(
            "mass-whitelist",
            "group" to group,
            "setting" to boolean.toString(),
            "issuer" to QuickAccess.nameOrConsole(sender)
        ).publish()
    }

    @AssignPermission
    @Subcommand("shutdown-scoped")
    @Description("Shutdown all servers in a group.")
    fun onShutdownScoped(
        sender: CommandSender,
        @Single group: String,
        @Single time: String
    )
    {
        sender.sendMessage("${CC.SEC}You've called a shutdown on all servers in the group ${CC.PRI}$group${CC.SEC}.")

        RedisHandler.buildMessage(
            "mass-reboot",
            "group" to group,
            "time" to time,
            "issuer" to QuickAccess.nameOrConsole(sender)
        ).publish()
    }

    @AssignPermission
    @Subcommand("shutdown-scoped-cancel")
    @Description("Cancel a shutdown on all servers in a group.")
    fun onShutdownCancelScoped(
        sender: CommandSender,
        @Single group: String
    )
    {
        sender.sendMessage("${CC.SEC}You've called a shutdown cancel on all servers in the group ${CC.PRI}$group${CC.SEC}.")

        RedisHandler.buildMessage(
            "mass-reboot-cancel",
            "group" to group,
            "issuer" to QuickAccess.nameOrConsole(sender)
        ).publish()
    }
}
