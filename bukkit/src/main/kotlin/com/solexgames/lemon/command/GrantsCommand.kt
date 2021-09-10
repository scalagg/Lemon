package com.solexgames.lemon.command

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.menu.grant.GrantViewMenu
import com.solexgames.lemon.player.enums.HistoryViewType
import com.solexgames.lemon.util.CubedCacheUtil
import com.solexgames.lemon.util.quickaccess.coloredName
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
 * @since 9/9/2021
 */
class GrantsCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grants")
    @CommandPermission("lemon.command.history.grants")
    fun onHistory(player: Player, uuid: UUID) {
        val name = CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.history.grants.other")) {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        val colored = coloredName(name)

        player.sendMessage("${CC.SEC}Viewing ${CC.PRI}${colored}'s${CC.SEC} grants...")

        Lemon.instance.grantHandler.fetchGrantsFor(uuid).thenAccept {
            if (it.isEmpty()) {
                player.sendMessage("${CC.RED}No grants found for ${CC.YELLOW}$colored${CC.RED}.")
                return@thenAccept
            }

            GrantViewMenu(
                uuid,
                HistoryViewType.TARGET_HIST,
                it
            ).openMenu(player)
        }
    }

    @Syntax("<player>")
    @CommandAlias("granthistory")
    @CommandCompletion("@players")
    @CommandPermission("lemon.command.staffhistory.grants")
    fun onStaffHistory(player: Player, uuid: UUID) {
        val name = CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.staffhistory.grants.other")) {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        val colored = coloredName(name)

        player.sendMessage("${CC.SEC}Viewing ${CC.PRI}${colored}'s${CC.SEC} grant history...")

        Lemon.instance.grantHandler.fetchGrantsByExecutor(uuid).thenAccept {
            if (it.isEmpty()) {
                player.sendMessage("${CC.RED}No grants found by ${CC.YELLOW}$colored${CC.RED}.")
                return@thenAccept
            }

            GrantViewMenu(
                uuid,
                HistoryViewType.STAFF_HIST,
                it
            ).openMenu(player)
        }
    }

}
