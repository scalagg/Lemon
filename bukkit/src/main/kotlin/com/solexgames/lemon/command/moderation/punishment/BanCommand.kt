package com.solexgames.lemon.command.moderation.punishment

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.util.QuickAccess.parseReason
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.util.time.Duration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
class BanCommand : BaseCommand() {

    @CommandAlias("ban|tempban|tban|b")
    @CommandPermission("lemon.command.ban")
    @Syntax("<player> <duration> [reason]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Unfair Advantage")
    fun onBan(sender: CommandSender, uuid: UUID, @Optional duration: Duration?, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") ?: false

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.BAN,
            duration = duration?.get() ?: Long.MAX_VALUE,
            reason = parseReason(reason), silent = silent
        )
    }
    @CommandAlias("reban|rb")
    @CommandPermission("lemon.command.ban")
    @Syntax("<player> <duration> [reason]")
    @CommandCompletion("@all-players 1d|1w|1mo|3mo|6mo|1y|perm|permanent Unfair Advantage")
    fun onReBan(sender: CommandSender, uuid: UUID, @Optional duration: Duration?, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") ?: false

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.BAN,
            duration = duration?.get() ?: Long.MAX_VALUE,
            reason = parseReason(reason), silent = silent,
            rePunishing = true
        )
    }

    @CommandAlias("recalculate-punishments")
    fun onRecalc(player: Player) {
        Lemon.instance.playerHandler.findPlayer(player).ifPresent {
            it.recalculatePunishments()

            it.activePunishments.forEach {
                player.sendMessage("${it.key.name} - ${it.value}")
            }
        }
    }
}
