package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.nameOrConsole
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
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
        ChatHandler.chatMuted = !ChatHandler.chatMuted

        val toggledTo = ChatHandler.chatMuted
        val coloredName = nameOrConsole(sender)
        val hidden = hiddenString != null && hiddenString == "-h"

        Bukkit.broadcastMessage("${if (toggledTo) CC.RED else CC.GREEN}Chat has been ${if (toggledTo) "disabled" else "enabled"} by ${if (hidden) "staff" else coloredName}${CC.GREEN}.")

        sendStaffMessage(
            sender,
            "$coloredName${CC.D_AQUA} has ${if (toggledTo) "${CC.RED}disabled" else "${CC.GREEN}enabled"}${CC.D_AQUA} chat.",
            true, QuickAccess.MessageType.NOTIFICATION
        )
    }
}
