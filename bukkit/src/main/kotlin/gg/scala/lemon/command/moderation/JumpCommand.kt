package gg.scala.lemon.command.moderation

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.fetchColoredName
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import gg.scala.lemon.util.QuickAccess.server
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 1/27/2022
 */
@AutoRegister
object JumpCommand : ScalaCommand()
{
    @CommandAlias("jump")
    @CommandCompletion("@players")
    @CommandPermission("lemon.command.jump")
    fun onJump(player: Player, target: AsyncLemonPlayer): CompletableFuture<Void>
    {
        return target.validatePlayers(player, false) { lemonPlayer ->
            server(lemonPlayer.uniqueId)
                .thenAcceptAsync {
                    if (it == null)
                        throw ConditionFailedException(
                            "${CC.YELLOW}${lemonPlayer.name}${CC.RED} is not online."
                        )

                    val coloredName = fetchColoredName(lemonPlayer.uniqueId)

                    sendStaffMessage(
                        "${coloredName(player)} ${CC.D_AQUA}jumped to ${CC.AQUA}${coloredName}${CC.D_AQUA}.", false
                    )

                    VelocityRedirectSystem
                        .redirect(player, it.id)
                }
                .join()
        }
    }
}
