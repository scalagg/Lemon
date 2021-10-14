package gg.scala.lemon.command.management

import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.menu.punishment.PunishmentViewMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object HistoryCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("history|c|check|hist")
    @CommandPermission("lemon.command.history.punishments")
    fun onHistory(player: Player, uuid: UUID) {
        val name = CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.history.punishments.other")) {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        player.sendMessage("${CC.SEC}Viewing ${CC.PRI}${coloredName(name)}'s${CC.SEC} history...")

        PunishmentHandler.fetchAllPunishmentsForTarget(uuid).thenAccept {
            if (it.isEmpty() && Lemon.instance.lemonWebData.serverName != "SolexGames") {
                player.sendMessage("${CC.RED}No punishments found for ${CC.YELLOW}${coloredName(name)}${CC.RED}.")
                return@thenAccept
            }

            PunishmentViewMenu(
                uuid, HistoryViewType.TARGET_HIST, it
            ).openMenu(player)
        }
    }

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("staffhistory|staffhist|csp|cp")
    @CommandPermission("lemon.command.staffhistory.punishments")
    fun onStaffHistory(player: Player, uuid: UUID) {
        val name = CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.staffhistory.punishments.other")) {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        player.sendMessage("${CC.SEC}Viewing ${CC.PRI}${coloredName(name)}'s${CC.SEC} staff history...")

        PunishmentHandler.fetchAllPunishmentsByExecutor(uuid).thenAccept {
            if (it.isEmpty() && Lemon.instance.lemonWebData.serverName != "SolexGames") {
                player.sendMessage("${CC.RED}No punishments found by ${CC.YELLOW}${coloredName(name)}${CC.RED}.")
                return@thenAccept
            }

            PunishmentViewMenu(
                uuid, HistoryViewType.STAFF_HIST, it
            ).openMenu(player)
        }
    }
}
