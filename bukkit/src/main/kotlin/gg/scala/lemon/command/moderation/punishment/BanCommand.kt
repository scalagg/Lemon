package gg.scala.lemon.command.moderation.punishment

import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.handler.PunishmentHandler.handleUnPunishmentForTargetPlayerGlobally
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess.isSilent
import gg.scala.lemon.util.QuickAccess.parseReason
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.time.Duration
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
object BanCommand : BaseCommand()
{

    @CommandAlias("ban|tban|tempban|b")
    @CommandPermission("lemon.command.ban")
    @Syntax("<player> <duration> [-s] [reason] [-s]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Unfair Advantage")
    fun onBan(
        sender: CommandSender, uuid: AsyncLemonPlayer,
        @Optional duration: Duration?, @Optional reason: String?
    ): CompletableFuture<Void>
    {
        val durationFinal = duration?.get() ?: Long.MAX_VALUE

        return uuid.validatePlayers(sender) {
            handlePunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.BAN,
                duration = durationFinal, reason = parseReason(reason),
                silent = isSilent(reason)
            )
        }
    }

    @CommandAlias("reban|rb")
    @CommandPermission("lemon.command.ban")
    @Syntax("<player> <duration> [-s] [reason] [-s]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Unfair Advantage")
    fun onReBan(
        sender: CommandSender, uuid: AsyncLemonPlayer,
        @Optional duration: Duration?, @Optional reason: String?
    ): CompletableFuture<Void>
    {
        val durationFinal = duration?.get() ?: Long.MAX_VALUE

        return uuid.validatePlayers(sender) {
            handlePunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.BAN,
                duration = durationFinal, reason = parseReason(reason),
                silent = isSilent(reason), rePunishing = true
            )
        }
    }

    @CommandAlias("unban|ub")
    @Syntax("<player> [-s] [reason] [-s]")
    @CommandCompletion("@all-players Appealed")
    @CommandPermission("lemon.command.ban.remove")
    fun onUnBan(
        sender: CommandSender, uuid: AsyncLemonPlayer,
        @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(sender) {
            handleUnPunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.BAN,
                reason = parseReason(reason, fallback = "Appealed"),
                silent = isSilent(reason),
            )
        }
    }
}
