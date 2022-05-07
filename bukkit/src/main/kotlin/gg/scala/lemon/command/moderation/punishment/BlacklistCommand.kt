package gg.scala.lemon.command.moderation.punishment

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.handler.PunishmentHandler.handleUnPunishmentForTargetPlayerGlobally
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.isSilent
import gg.scala.lemon.util.QuickAccess.parseReason
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.*
import gg.scala.commons.acf.annotation.Optional
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
@AutoRegister
object BlacklistCommand : ScalaCommand()
{
    @Syntax("<player> [-s] [reason] [-s]")
    @CommandAlias("blacklist|bl")
    @CommandPermission("lemon.command.blacklist")
    @CommandCompletion("@all-players Unfair Advantage")
    fun onBlacklist(
        sender: CommandSender,
        uuid: AsyncLemonPlayer, @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(sender, true) {
            handlePunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.BLACKLIST,
                duration = Long.MAX_VALUE,
                reason = parseReason(reason),
                silent = isSilent(reason),
            )
        }
    }

    @Syntax("<player> [-s] [reason] [-s]")
    @CommandAlias("reblacklist|rbl")
    @CommandPermission("lemon.command.blacklist")
    @CommandCompletion("@all-players Unfair Advantage")
    fun onReBlacklist(
        sender: CommandSender,
        uuid: AsyncLemonPlayer, @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(sender, true) {
            handlePunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.BLACKLIST,
                duration = Long.MAX_VALUE,
                reason = parseReason(reason),
                silent = isSilent(reason),
                rePunishing = true
            )
        }
    }

    @CommandAlias("unblacklist|ubl")
    @Syntax("<player> [-s] [reason] [-s]")
    @CommandCompletion("@all-players Appealed")
    @CommandPermission("lemon.command.blacklist.remove")
    fun onUnBlacklist(
        sender: CommandSender,
        uuid: AsyncLemonPlayer,
        @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(sender, true) {
            handleUnPunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.BLACKLIST,
                reason = parseReason(reason, fallback = "Appealed"),
                silent = isSilent(reason)
            )
        }
    }
}
