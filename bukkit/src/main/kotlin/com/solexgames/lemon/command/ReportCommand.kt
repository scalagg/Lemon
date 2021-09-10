package com.solexgames.lemon.command

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.menu.report.ReportMenu
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.quickaccess.MessageType
import com.solexgames.lemon.util.quickaccess.remaining
import com.solexgames.lemon.util.quickaccess.sendStaffMessage
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/6/2021
 */

class ReportCommand : BaseCommand() {

    @CommandAlias("report")
    @Syntax("<player>")
    fun onDefault(player: Player, target: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.cooldowns["report"]?.isActive() == true) {
            val remaining = lemonPlayer.cooldowns["report"]?.let { remaining(it) }
            player.sendMessage("${CC.RED}You must wait $remaining seconds before submitting another report.")

            return
        }

        if (target.uniqueId == player.uniqueId) {
            throw ConditionFailedException("You're not allowed to report yourself.")
        }

        ReportMenu(target).openMenu(player)
    }
}
