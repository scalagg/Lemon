package com.solexgames.lemon.command

import com.solexgames.lemon.Lemon
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
class ShutdownCommand: BaseCommand() {

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("initiate|start")
    fun onInitiate(player: Player, @Name("time") time: Duration) {
        if (time.isPermanent()) {
            throw ConditionFailedException("That duration is too long.")
        }

        val seconds = try {
            (time.get() / 1000).toInt()
        } catch (e: Exception) {
            throw ConditionFailedException("That duration is too long.")
        }

        Lemon.instance.serverHandler.initiateShutdown(player, seconds)
    }

    @Subcommand("status")
    fun onStatus(player: Player) {
        val shutdown = Lemon.instance.serverHandler.shutdownRunnable

        if (shutdown != null) {
            player.sendMessage("${CC.SEC}The server is scheduled to shutdown in ${CC.PRI}${TimeUtil.formatIntoDetailedString(shutdown.seconds)}${CC.SEC}.")
        } else {
            player.sendMessage("${CC.RED}There is currently no scheduled shutdown.")
        }
    }

    @Subcommand("cancel|stop")
    fun onCancel(player: Player) {
        Lemon.instance.serverHandler.cancelShutdown(player)
    }

}
