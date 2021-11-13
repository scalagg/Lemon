package gg.scala.lemon.command

import gg.scala.lemon.LemonConstants
import gg.scala.lemon.util.QuickAccess.sendGlobalBroadcast
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color.translate
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
class AlertCommand : BaseCommand()
{
    @CommandAlias("alert")
    @CommandPermission("op")
    @Syntax("<message> [-r]")
    fun onAlert(sender: CommandSender, message: String)
    {
        var finalMessage = translate(message)

        for (flag in LemonConstants.FLAGS)
        {
            if (message.contains(" -${flag.key}"))
            {
                finalMessage = message.replace(" -${flag.key}", "")
                finalMessage = flag.value.invoke(finalMessage)
            }
        }

        sendGlobalBroadcast(finalMessage).thenRun {
            sender.sendMessage("${CC.GREEN}Your alert has been dispatched.")
        }
    }
}
