package com.solexgames.lemon.command.conversation

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 29/08/2021 19:40
 */
class ReplyCommand: BaseCommand() {

    @CommandAlias("reply|r|respond")
    fun onReply(player: Player, @Optional message: String?) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

        message?.let {
            lemonPlayer.lastRecipient?.let {
                player.sendMessage("${CC.SEC}You're currently messaging ${CC.PRI}$it${CC.SEC}.")
            } ?: let {
                player.sendMessage("${CC.RED}You're currently not messaging anyone.")
            }
        } ?: let {
            player.performCommand("msg ${lemonPlayer.lastRecipient} $message")
        }
    }
}
