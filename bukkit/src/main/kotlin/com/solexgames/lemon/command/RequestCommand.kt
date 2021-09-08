package com.solexgames.lemon.command

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.quickaccess.MessageType
import com.solexgames.lemon.util.quickaccess.coloredName
import com.solexgames.lemon.util.quickaccess.remaining
import com.solexgames.lemon.util.quickaccess.sendStaffMessage
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Default
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
@CommandAlias("request")
class RequestCommand : BaseCommand() {

    @Default
    @Syntax("[message]")
    fun onDefault(player: Player, message: String) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

        if (lemonPlayer.requestCooldown.isActive()) {
            val remaining = remaining(lemonPlayer.requestCooldown)
            player.sendMessage("${CC.RED}You must wait $remaining seconds before submitting another request.")

            return
        }

        sendStaffMessage(
            player,
            "${CC.YELLOW}${coloredName(player)} ${CC.RED}submitted a request: ${CC.YELLOW}$message${CC.RED}.",
            true,
            MessageType.NOTIFICATION
        ).whenComplete { _, throwable ->
            if (throwable != null) {
                player.sendMessage("${CC.RED}Something went wrong while submitting your request, try again later.")
            } else {
                lemonPlayer.requestCooldown = Cooldown(60000L)

                player.sendMessage("${CC.GREEN}Your request has been submitted.")
            }
        }
    }
}
