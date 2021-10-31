package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.ChatHandler
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.nameOrConsole
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
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
class ChatCommand : BaseCommand() {

    @Syntax("[-h]")
    @CommandAlias("mutechat|silencechat")
    @CommandPermission("lemon.command.mutechat")
    fun onMuteChat(sender: CommandSender, @Optional hiddenString: String?) {
        if (ChatHandler.chatMuted)
        {
            throw ConditionFailedException("The chat is already silenced. Use ${CC.BOLD}/unmutechat${CC.RED} to unmute the chat!")
        }

        ChatHandler.chatMuted = true

        val coloredName = nameOrConsole(sender)
        val hidden = hiddenString != null && hiddenString == "-h"

        Bukkit.broadcastMessage("${CC.GREEN}Chat has been enabled by ${if (hidden) "staff" else coloredName}${CC.GREEN}.")

        sendStaffMessage(
            sender,
            "$coloredName${CC.D_AQUA} has ${"${CC.GREEN}enabled"}${CC.D_AQUA} chat.",
            true, QuickAccess.MessageType.NOTIFICATION
        )
    }

    @Syntax("[-h]")
    @CommandAlias("mutechat|silencechat")
    @CommandPermission("lemon.command.mutechat")
    fun onUnMuteChat(sender: CommandSender, @Optional hiddenString: String?) {
        if (!ChatHandler.chatMuted)
        {
            throw ConditionFailedException("The chat is not silenced. Use ${CC.BOLD}/mutechat${CC.RED} to mute the chat!")
        }

        ChatHandler.chatMuted = false

        val coloredName = nameOrConsole(sender)
        val hidden = hiddenString != null && hiddenString == "-h"

        Bukkit.broadcastMessage("${CC.RED}Chat has been disabled by ${if (hidden) "staff" else coloredName}${CC.RED}.")

        sendStaffMessage(
            sender,
            "$coloredName${CC.D_AQUA} has ${"${CC.RED}disabled"}${CC.D_AQUA} chat.",
            true, QuickAccess.MessageType.NOTIFICATION
        )
    }

    @Syntax("<seconds> [-h]")
    @CommandAlias("slowchat")
    @CommandPermission("lemon.command.slowchat")
    fun onSlowChat(sender: CommandSender, seconds: Int, @Optional hiddenString: String?) {
        if (seconds < 0)
        {
            throw ConditionFailedException("You cannot use a negative number.")
        }

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
