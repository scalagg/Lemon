package com.solexgames.lemon.command.management

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.QuickAccess.coloredName
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.menu.grant.GrantViewMenu
import com.solexgames.lemon.menu.punishment.PunishmentViewMenu
import com.solexgames.lemon.player.enums.HistoryViewType
import com.solexgames.lemon.util.CubedCacheUtil
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
class HistoryCommand : BaseCommand() {

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

        Lemon.instance.punishmentHandler.fetchAllPunishmentsForTarget(uuid).thenAccept {
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

        Lemon.instance.punishmentHandler.fetchAllPunishmentsByExecutor(uuid).thenAccept {
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
