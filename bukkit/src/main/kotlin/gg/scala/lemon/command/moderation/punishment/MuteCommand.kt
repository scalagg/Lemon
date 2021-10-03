package gg.scala.lemon.command.moderation.punishment

import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.handler.PunishmentHandler.handleUnPunishmentForTargetPlayerGlobally
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess.parseReason
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.time.Duration
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
class MuteCommand : BaseCommand() {

    @CommandAlias("mute|tmute|tempmute")
    @CommandPermission("lemon.command.mute")
    @Syntax("<player> <duration> [reason]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Spamming")
    fun onMute(sender: CommandSender, uuid: UUID, @Optional duration: Duration?, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") ?: false
        val durationFinal = duration?.get() ?: Long.MAX_VALUE

        if (durationFinal == Long.MAX_VALUE && !sender.hasPermission("lemon.command.mute.permanent")) {
            throw ConditionFailedException("You do not have permission to issue permanent mutes.")
        }

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.MUTE,
            duration = durationFinal, reason = parseReason(reason, fallback = "Spamming"),
            silent = silent
        )
    }

    @CommandAlias("remute|rm")
    @CommandPermission("lemon.command.mute")
    @Syntax("<player> <duration> [reason]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Spamming")
    fun onReMute(sender: CommandSender, uuid: UUID, @Optional duration: Duration?, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") ?: false
        val durationFinal = duration?.get() ?: Long.MAX_VALUE

        if (durationFinal == Long.MAX_VALUE && !sender.hasPermission("lemon.command.mute.permanent")) {
            throw ConditionFailedException("You do not have permission to issue permanent mutes.")
        }

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.MUTE,
            duration = durationFinal, reason = parseReason(reason, fallback = "Spamming"),
            silent = silent, rePunishing = true
        )
    }

    @CommandAlias("unmute|um")
    @Syntax("<player> <duration> [reason]")
    @CommandCompletion("@all-players Appealed")
    @CommandPermission("lemon.command.mute.remove")
    fun onUnMute(sender: CommandSender, uuid: UUID, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") ?: false

        handleUnPunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.MUTE,
            reason = parseReason(reason, fallback = "Appealed"), silent = silent,
        )
    }
}
