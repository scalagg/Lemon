package gg.scala.lemon.command

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.util.QuickAccess.sendGlobalBroadcast
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color.translate
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
@AutoRegister
object AlertCommand : ScalaCommand()
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
