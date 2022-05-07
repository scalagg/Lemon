package gg.scala.lemon.command.management.invalidation

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.player.enums.InvalidationType
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@AutoRegister
object InvalidateGrantCommand : ScalaCommand()
{
    @Syntax("<type> [player]")
    @CommandAlias("invalidategrants")
    @CommandPermission("lemon.command.invalidategrants")
    fun onInvalidate(sender: CommandSender, invalidationType: InvalidationType, uuid: UUID)
    {
        val completableFuture = if (invalidationType == InvalidationType.ISSUED)
        {
            GrantHandler.invalidateAllGrantsBy(uuid, sender)
        } else
        {
            GrantHandler.invalidateAllGrantsFor(uuid, sender)
        }

        sender.sendMessage("${CC.SEC}Starting grant invalidation...")

        completableFuture.thenRun {
            sender.sendMessage("${CC.GREEN}Finished grant invalidation.")
        }
    }

}
