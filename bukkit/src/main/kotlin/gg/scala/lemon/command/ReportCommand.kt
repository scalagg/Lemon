package gg.scala.lemon.command

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.menu.report.ReportMenu
import gg.scala.lemon.util.QuickAccess.remaining
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/6/2021
 */

class ReportCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandAlias("report")
    @CommandCompletion("@all-players")
    fun onDefault(player: Player, target: OnlinePlayer) {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.cooldowns["report"]?.isActive() == true) {
            val remaining = lemonPlayer.cooldowns["report"]?.let { remaining(it) }

            player.sendMessage("${CC.RED}You must wait $remaining seconds before submitting another report.")
            return
        }

        if (target.player.uniqueId == player.uniqueId) {
            throw ConditionFailedException("You're not allowed to report yourself.")
        }

        ReportMenu(target.player).openMenu(player)
    }
}
