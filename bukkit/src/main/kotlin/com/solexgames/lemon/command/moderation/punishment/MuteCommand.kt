package com.solexgames.lemon.command.moderation.punishment

import com.solexgames.lemon.handler.PunishmentHandler
import com.solexgames.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import com.solexgames.lemon.handler.PunishmentHandler.handleUnPunishmentForTargetPlayerGlobally
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.util.QuickAccess.parseReason
import net.evilblock.cubed.acf.BaseCommand
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

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.MUTE,
            duration = duration?.get() ?: Long.MAX_VALUE,
            reason = parseReason(reason), silent = silent
        )
    }

    @CommandAlias("remute|rm")
    @CommandPermission("lemon.command.mute")
    @Syntax("<player> <duration> [reason]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Spamming")
    fun onReMute(sender: CommandSender, uuid: UUID, @Optional duration: Duration?, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") ?: false

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.MUTE,
            duration = duration?.get() ?: Long.MAX_VALUE,
            reason = parseReason(reason), silent = silent,
            rePunishing = true
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