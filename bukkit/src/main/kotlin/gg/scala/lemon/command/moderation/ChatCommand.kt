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
import org.bukkit.util.StringUtil
import java.util.concurrent.ThreadLocalRandom

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
object ChatCommand : BaseCommand() {

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

    @Syntax("<seconds> [-h]")
    @CommandAlias("slowchat")
    @CommandPermission("lemon.command.slowchat")
    fun onSlowChat(sender: CommandSender, seconds: Int, @Optional hiddenString: String?) {
        ChatHandler.slowChatTime = seconds

        val toggledTo = ChatHandler.chatMuted
        val coloredName = nameOrConsole(sender)
        val hidden = hiddenString != null && hiddenString == "-h"

        Bukkit.broadcastMessage("${if (toggledTo) CC.RED else CC.GREEN}Chat has been slowed to $seconds seconds by ${if (hidden) "staff" else coloredName}${CC.GREEN}.")

        sendStaffMessage(
            sender,
            "$coloredName${CC.D_AQUA} has slowed the chat to $seconds seconds.",
            true, QuickAccess.MessageType.NOTIFICATION
        )
    }

    @Syntax("[-h]")
    @CommandAlias("clearchat|cc")
    @CommandPermission("lemon.command.clearchat")
    fun onClearChat(sender: CommandSender, @Optional hiddenString: String?) {
        val coloredName = nameOrConsole(sender)
        val hidden = hiddenString != null && hiddenString == "-h"

        Bukkit.getOnlinePlayers()
            .filter { !it.hasPermission("lemon.staff") }
            .forEach {
                for (int in 0..250) {
                    it.sendMessage(
                        " ".repeat(
                            ThreadLocalRandom.current().nextInt(1, 50)
                        )
                    )
                }
            }

        Bukkit.broadcastMessage("${CC.GREEN}Chat has been cleared by ${if (hidden) "staff" else coloredName}${CC.GREEN}.")

        sendStaffMessage(
            sender,
            "$coloredName${CC.D_AQUA} has cleared chat.",
            true, QuickAccess.MessageType.NOTIFICATION
        )
    }
}
