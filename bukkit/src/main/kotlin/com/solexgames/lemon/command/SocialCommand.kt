package com.solexgames.lemon.command

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
class SocialCommand : BaseCommand() {

    @CommandAlias("discord")
    fun onDiscord(sender: CommandSender) {
        sender.sendMessage(
            "${CC.SEC}Discord: ${CC.PRI}${Lemon.instance.lemonWebData.discord}"
        )
    }

    @CommandAlias("twitter")
    fun onTwitter(sender: CommandSender) {
        sender.sendMessage(
            "${CC.SEC}Twitter: ${CC.PRI}${Lemon.instance.lemonWebData.twitter}"
        )
    }

    @CommandAlias("website")
    fun onWebsite(sender: CommandSender) {
        sender.sendMessage(
            "${CC.SEC}Twitter: ${CC.PRI}${Lemon.instance.lemonWebData.domain}"
        )
    }

    @CommandAlias("store")
    fun onStore(sender: CommandSender) {
        sender.sendMessage(
            "${CC.SEC}Twitter: ${CC.PRI}${Lemon.instance.lemonWebData.store}"
        )
    }
}
