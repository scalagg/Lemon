package gg.scala.lemon.command.moderation.punishment

import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess.parseReason
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
class KickCommand : BaseCommand() {

    @Syntax("<player> [reason]")
    @CommandAlias("kick|lattabitch|k")
    @CommandPermission("lemon.command.kick")
    @CommandCompletion("@all-players Annoying|Latta Bitch")
    fun onKick(sender: CommandSender, uuid: UUID, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") ?: false

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.KICK,
            duration = 1L, reason = parseReason(reason),
            silent = silent
        )
    }
}