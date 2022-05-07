package gg.scala.lemon.command.management

import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.ServerHandler
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.*
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
object ShutdownCommand : ScalaCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("initiate|start")
    @Description("Initiate a server shutdown.")
    fun onInitiate(player: Player, @Name("time") time: Duration)
    {
        if (time.isPermanent())
        {
            throw ConditionFailedException("That duration is too long.")
        }

        val seconds = try
        {
            (time.get() / 1000).toInt()
        } catch (e: Exception)
        {
            throw ConditionFailedException("You must .")
        }

        player.sendMessage("${CC.GREEN}The shutdown has been initiated.")

        ServerHandler.initiateShutdown(seconds)
    }

    @Subcommand("status")
    @Description("View the status of the current shutdown.")
    fun onStatus(player: Player)
    {
        val shutdown = ServerHandler.shutdownRunnable

        if (shutdown != null)
        {
            player.sendMessage(
                "${CC.RED}The server is scheduled to shutdown in ${CC.YELLOW}${
                    TimeUtil.formatIntoDetailedString(
                        shutdown.seconds
                    )
                }${CC.RED}."
            )
        } else
        {
            player.sendMessage("${CC.RED}There is currently no scheduled shutdown.")
        }
    }

    @Subcommand("cancel|stop")
    @Description("Cancel the current shutdown.")
    fun onCancel(player: Player)
    {
        ServerHandler.cancelShutdown()
    }

}
