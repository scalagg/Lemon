package gg.scala.lemon.command.moderation.punishment

import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.online
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
class KickCommand : BaseCommand()
{
    @CommandAlias("kick|k")
    @Syntax("<player> [-s] [reason] [-s]")
    @CommandPermission("lemon.command.kick")
    @CommandCompletion("@all-players Camping")
    fun onKick(
        sender: CommandSender,
        uuid: AsyncLemonPlayer,
        @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(sender) {
            val online = online(it.uniqueId).join()

            if (!online)
            {
                throw ConditionFailedException("That player isn't logged onto our network.")
            }

            PunishmentHandler.handlePunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.KICK, duration = 1L,
                reason = QuickAccess.parseReason(reason, fallback = "Camping"),
                silent = QuickAccess.isSilent(reason),
            )
        }
    }
}
