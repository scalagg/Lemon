package com.solexgames.lemon.command

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonAPI
import com.solexgames.lemon.LemonAPI.coloredName
import com.solexgames.lemon.LemonAPI.remaining
import com.solexgames.lemon.LemonAPI.sendStaffMessage
import com.solexgames.lemon.util.other.Cooldown
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
            LemonAPI.MessageType.NOTIFICATION
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
