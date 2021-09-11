package com.solexgames.lemon.command.conversation

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 29/08/2021 19:40
 */
class ReplyCommand : BaseCommand() {

    @CommandAlias("reply|r")
    @CommandCompletion("@players-uv")
    fun onReply(player: Player, @Optional message: String?) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

        lemonPlayer.getMetadata("last-recipient")?.let {
            val recipientUsername = CubedCacheUtil.fetchName(it.asUuid())

            message?.let {
                player.performCommand("message $recipientUsername $message")
            } ?: let {
                player.sendMessage("${CC.SEC}You're currently messaging ${CC.PRI}$recipientUsername${CC.SEC}.")
            }
        } ?: let {
            player.sendMessage("${CC.RED}You're not messaging anyone.")
        }
    }
}
