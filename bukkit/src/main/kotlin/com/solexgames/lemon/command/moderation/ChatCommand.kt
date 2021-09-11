package com.solexgames.lemon.command.moderation

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.quickaccess.MessageType
import com.solexgames.lemon.util.quickaccess.coloredNameOrConsole
import com.solexgames.lemon.util.quickaccess.sendStaffMessage
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
class ChatCommand : BaseCommand() {

    @Syntax("[-h]")
    @CommandAlias("mutechat|mc")
    @CommandPermission("lemon.command.mutechat")
    fun onMuteChat(sender: CommandSender, @Optional hiddenString: String?) {
        Lemon.instance.chatHandler.chatMuted = !Lemon.instance.chatHandler.chatMuted

        val toggledTo = Lemon.instance.chatHandler.chatMuted
        val coloredName = coloredNameOrConsole(sender)
        val hidden = hiddenString != null && hiddenString == "-h"

        Bukkit.broadcastMessage("${if (toggledTo) CC.RED else CC.GREEN}Chat has been ${if (toggledTo) "disabled" else "enabled"} by ${if (hidden) "staff" else coloredName}.")

        sendStaffMessage(
            sender,
            "$coloredName ${CC.D_AQUA}${if (toggledTo) "${CC.RED}disabled" else "${CC.GREEN}enabled"}${CC.D_AQUA} chat.",
            true, MessageType.NOTIFICATION
        )
    }
}
