package gg.scala.lemon.command

import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.sendGlobalBroadcast
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import org.bukkit.command.CommandSender
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.Color.translate

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
class AlertCommand : BaseCommand()
{
    companion object
    {
        @JvmStatic
        val FLAGS = mutableMapOf<String, (String) -> String>(
            "r" to {
                "${CC.D_GRAY}[${CC.D_RED}Alert${CC.D_GRAY}] ${CC.RESET}$it"
            }
        )
    }

    @CommandAlias("alert")
    @CommandPermission("op")
    @Syntax("<message> [-r]")
    fun onAlert(sender: CommandSender, message: String)
    {
        var finalMessage = translate(message)

        for (flag in FLAGS)
        {
            if (message.contains(" -${flag.key}"))
            {
                finalMessage = message.replace(" -$${flag.key}", "")
                finalMessage = flag.value.invoke(finalMessage)
            }
        }

        sendGlobalBroadcast(finalMessage).thenRun {
            sender.sendMessage("${CC.GREEN}Your alert has been dispatched.")
        }
    }
}
