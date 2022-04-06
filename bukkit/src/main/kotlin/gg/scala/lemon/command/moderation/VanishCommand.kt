package gg.scala.lemon.command.moderation

import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/15/2021
 */
object VanishCommand : BaseCommand() {

    @CommandAlias("vanish|v|tv|togglevanish")
    @CommandPermission("lemon.command.vanish")
    @CommandCompletion("0|10|100|1000 @all-players")
    fun onVanish(sender: Player, @Optional priority: Int?, @Optional target: LemonPlayer?) {
        if (target != null) {
            if (!sender.uniqueId.equals(target.bukkitPlayer!!.uniqueId) && !sender.hasPermission("lemon.command.vanish.other")) {
                throw ConditionFailedException("${CC.RED}You do not have permission to vanish others!")
            }
        }

        val playerToVanish = if (target != null) target.bukkitPlayer!! else sender
        val lemonPlayer = PlayerHandler.findPlayer(playerToVanish).orElse(null)
        val youOrNot = if (target != null) "${CC.YELLOW}${playerToVanish.name}${CC.RED} does" else "You do"
        val youHave = if (target != null) "${coloredName(playerToVanish)}${CC.SEC} has" else "${CC.SEC}You've"

        if (!playerToVanish.isOp && lemonPlayer.activeGrant!!.getRank().weight < (priority ?: lemonPlayer.activeGrant!!.getRank().weight)) {
            throw ConditionFailedException("${CC.RED}$youOrNot not have permission to vanish with that priority.")
        }

        if (!playerToVanish.hasMetadata("vanished")) {
            PlayerHandler.vanishPlayer(
                playerToVanish,
                power = priority ?: lemonPlayer.activeGrant!!.getRank().weight
            )

            sender.sendMessage("${LemonConstants.AUTH_PREFIX}$youHave been ${CC.GREEN}vanished${CC.SEC} with a power of ${CC.WHITE}${priority ?: lemonPlayer.activeGrant!!.getRank().weight}.")
        } else {
            PlayerHandler.unvanishPlayer(playerToVanish)

            sender.sendMessage("${LemonConstants.AUTH_PREFIX}$youHave been ${CC.RED}un-vanished${CC.SEC}.")
        }
    }
}
