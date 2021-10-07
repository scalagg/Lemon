package gg.scala.lemon.command.management

import gg.scala.lemon.handler.ServerHandler
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.Duration
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 27/08/2021 20:02
 */
@CommandAlias("shutdown|reboot")
@CommandPermission("lemon.command.shutdown")
class ShutdownCommand : BaseCommand() {

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("initiate|start")
    @Description("Initiate a server shutdown.")
    fun onInitiate(player: Player, @Name("time") time: Duration) {
        if (time.isPermanent()) {
            throw ConditionFailedException("That duration is too long.")
        }

        val seconds = try {
            (time.get() / 1000).toInt()
        } catch (e: Exception) {
            throw ConditionFailedException("That duration is too long.")
        }

        ServerHandler.initiateShutdown(seconds)
    }

    @Subcommand("status")
    @Description("View the status of the current shutdown.")
    fun onStatus(player: Player) {
        val shutdown = ServerHandler.shutdownRunnable

        if (shutdown != null) {
            player.sendMessage(
                "${CC.SEC}The server is scheduled to shutdown in ${CC.PRI}${
                    TimeUtil.formatIntoDetailedString(
                        shutdown.seconds
                    )
                }${CC.SEC}."
            )
        } else {
            player.sendMessage("${CC.RED}There is currently no scheduled shutdown.")
        }
    }

    @Subcommand("cancel|stop")
    @Description("Cancel the current shutdown.")
    fun onCancel(player: Player) {
        ServerHandler.cancelShutdown()
    }

}
