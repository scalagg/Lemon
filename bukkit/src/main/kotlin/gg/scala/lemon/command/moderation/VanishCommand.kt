package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/15/2021
 */
class VanishCommand : BaseCommand() {

    @Syntax("[priority] [target]")
    @CommandAlias("vanish|v|tv|togglevanish")
    @CommandPermission("lemon.command.vanish")
    fun onVanish(sender: Player, @Optional priority: Int?, @Optional target: OnlinePlayer?) {
        if (target != null) {
            if (!sender.uniqueId.equals(target.player.uniqueId) && !sender.hasPermission("lemon.command.vanish.other")) {
                throw ConditionFailedException("${CC.RED}You do not have permission to vanish other players!")
            }
        }

        val playerToVanish = if (target != null) target.player else sender
        val lemonPlayer = PlayerHandler.findPlayer(playerToVanish).orElse(null)
        val youOrNot = if (target != null) "${CC.YELLOW}${playerToVanish.name}${CC.RED} does" else "You do"
        val youHave = if (target != null) "${coloredName(playerToVanish)}${CC.SEC} has" else "${CC.SEC}You've"

        if (!playerToVanish.isOp && lemonPlayer.activeGrant!!.getRank().weight > (priority ?: 0)) {
            throw ConditionFailedException("${CC.RED}$youOrNot not have permission to vanish with a priority higher than their current rank!")
        }

        if (!playerToVanish.hasMetadata("vanished")) {
            PlayerHandler.vanishPlayer(
                playerToVanish,
                power = priority ?: 0
            )

            sender.sendMessage("$youHave been ${CC.GREEN}vanished${CC.SEC} with a priority of ${CC.WHITE}${priority ?: 0}.")
        } else {
            PlayerHandler.unvanishPlayer(playerToVanish)

            sender.sendMessage("$youHave been ${CC.RED}un-vanished${CC.SEC}.")
        }
    }
}
