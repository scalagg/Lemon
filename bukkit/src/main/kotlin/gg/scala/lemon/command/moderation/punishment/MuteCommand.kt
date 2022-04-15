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
@AutoRegister
object MuteCommand : ScalaCommand()
{
    @CommandAlias("mute|tmute|tempmute")
    @CommandPermission("lemon.command.mute")
    @Syntax("<player> <duration> [-s] [reason] [-s]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Spamming")
    fun onMute(
        sender: CommandSender, uuid: AsyncLemonPlayer,
        @Optional duration: Duration?, @Optional reason: String?
    ): CompletableFuture<Void>
    {
        val durationFinal = duration?.get() ?: Long.MAX_VALUE

        return uuid.validatePlayers(sender, true) {
            handlePunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.MUTE,
                duration = durationFinal,
                reason = parseReason(reason, fallback = "Spamming"),
                silent = isSilent(reason),
            )
        }
    }

    @CommandAlias("remute|rm")
    @CommandPermission("lemon.command.mute")
    @Syntax("<player> <duration> [-s] [reason] [-s]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Spamming")
    fun onReMute(
        sender: CommandSender, uuid: AsyncLemonPlayer,
        @Optional duration: Duration?, @Optional reason: String?
    ): CompletableFuture<Void>
    {
        val durationFinal = duration?.get() ?: Long.MAX_VALUE

        return uuid.validatePlayers(sender, true) {
            handlePunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.MUTE,
                duration = durationFinal,
                reason = parseReason(reason, fallback = "Spamming"),
                silent = isSilent(reason), rePunishing = true
            )
        }
    }

    @CommandAlias("unmute|um")
    @Syntax("<player> <duration> [-s] [reason] [-s]")
    @CommandCompletion("@all-players Appealed")
    @CommandPermission("lemon.command.mute.remove")
    fun onUnMute(
        sender: CommandSender,
        uuid: AsyncLemonPlayer,
        @Optional reason: String?
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(sender, true) {
            handleUnPunishmentForTargetPlayerGlobally(
                issuer = sender, uuid = it.uniqueId,
                category = PunishmentCategory.MUTE,
                reason = parseReason(reason, fallback = "Appealed"),
                silent = isSilent(reason),
            )
        }
    }
}
