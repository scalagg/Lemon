package gg.scala.lemon.command.moderation.punishment

import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.handler.PunishmentHandler.handleUnPunishmentForTargetPlayerGlobally
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess.parseReason
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.acf.annotation.Optional
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
object BlacklistCommand : BaseCommand() {

    @Syntax("<player> [-s] [reason] [-s]")
    @CommandAlias("blacklist|bl")
    @CommandPermission("lemon.command.blacklist")
    @CommandCompletion("@all-players Unfair Advantage")
    fun onBlacklist(sender: CommandSender, uuid: UUID, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") == true || reason?.startsWith("-s ") ?: false

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.BLACKLIST,
            duration = Long.MAX_VALUE,
            reason = parseReason(reason), silent = silent
        )
    }

    @Syntax("<player> [-s] [reason] [-s]")
    @CommandAlias("reblacklist|rbl")
    @CommandPermission("lemon.command.blacklist")
    @CommandCompletion("@all-players Unfair Advantage")
    fun onReBlacklist(sender: CommandSender, uuid: UUID, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") == true || reason?.startsWith("-s ") ?: false

        handlePunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.BLACKLIST,
            duration = Long.MAX_VALUE,
            reason = parseReason(reason), silent = silent,
            rePunishing = true
        )
    }

    @CommandAlias("unblacklist|ubl")
    @Syntax("<player> [-s] [reason] [-s]")
    @CommandCompletion("@all-players Appealed")
    @CommandPermission("lemon.command.blacklist.remove")
    fun onUnBlacklist(sender: CommandSender, uuid: UUID, @Optional reason: String?) {
        val silent = reason?.endsWith(" -s") == true || reason?.startsWith("-s ") ?: false

        handleUnPunishmentForTargetPlayerGlobally(
            issuer = sender, uuid = uuid,
            category = PunishmentCategory.BLACKLIST,
            reason = parseReason(reason, fallback = "Appealed"), silent = silent,
        )
    }
}
