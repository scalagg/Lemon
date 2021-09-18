package gg.scala.lemon.command

import gg.scala.lemon.Lemon
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.remaining
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import gg.scala.lemon.util.other.Cooldown
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
class RequestCommand : BaseCommand() {

    @CommandAlias("request")
    fun onDefault(player: Player, message: String) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.cooldowns["request"]?.isActive() == true) {
            val remaining = lemonPlayer.cooldowns["request"]?.let { remaining(it) }

            player.sendMessage("${CC.RED}You must wait $remaining seconds before submitting another request.")
            return
        }

        sendStaffMessage(
            player,
            "${CC.YELLOW}${coloredName(player)} ${CC.RED}submitted a request: ${CC.YELLOW}$message${CC.RED}.",
            true,
            QuickAccess.MessageType.NOTIFICATION
        ).whenComplete { _, throwable ->
            if (throwable != null) {
                player.sendMessage("${CC.RED}Something went wrong while submitting your request, try again later.")
            } else {
                lemonPlayer.cooldowns["request"] = Cooldown(60000L)

                player.sendMessage("${CC.GREEN}Your request has been submitted.")
            }
        }
    }
}
