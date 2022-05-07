package gg.scala.lemon.command.moderation.punishment

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
@AutoRegister
object WarnCommand : ScalaCommand()
{
    @CommandAlias("warn")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.warn")
    fun onWarn(
        sender: CommandSender,
        target: AsyncLemonPlayer,
        reason: String
    ): CompletableFuture<Void>
    {
        return target.validatePlayers(sender, true) {
            PunishmentHandler
                .handleWarning(
                    sender, it.uniqueId, reason
                )
                .join()
        }
    }
}
