package gg.scala.lemon.command.moderation

import gg.scala.lemon.redirection.impl.VelocityRedirectSystem
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.fetchColoredName
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import gg.scala.lemon.util.QuickAccess.server
import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 1/27/2022
 */
class JumpCommand : BaseCommand()
{
    @CommandAlias("jump")
    @CommandCompletion("@players")
    @CommandPermission("lemon.command.jump")
    fun onJump(player: Player, target: UUID): CompletableFuture<Void>
    {
        player.sendMessage("${CC.GREEN}Locating player ${CC.YELLOW}${target.username()}${CC.GREEN}...")

        return server(target)
            .thenAcceptAsync {
                if (it == null)
                    throw ConditionFailedException(
                        "${CC.YELLOW}${target.username()}${CC.RED} is not online."
                    )

                val coloredName = fetchColoredName(target)

                sendStaffMessage(
                    "${coloredName(player)} ${CC.D_AQUA}jumped to ${CC.AQUA}${coloredName}${CC.D_AQUA}.", false
                )

                VelocityRedirectSystem
                    .redirect(player, it.id)
            }
    }
}
