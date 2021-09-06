package com.solexgames.lemon.command

import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.menu.punishment.PunishmentViewMenu
import com.solexgames.lemon.player.enums.PunishmentViewType
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.MessageKeys
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
class HistoryCommand: BaseCommand() {

    @Syntax("<player>")
    @CommandAlias("history|c|check|hist")
    @CommandPermission("lemon.command.history")
    @CommandCompletion("@players")
    fun onHistory(player: Player, uuid: UUID) {
        CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.history.other")) {
            player.sendMessage(LemonConstants.NO_PERMISSION)
            return
        }

        PunishmentViewMenu(uuid, PunishmentViewType.TARGET_HIST).openMenu(player)
    }

    @Syntax("<player>")
    @CommandAlias("staffhistory|staffhist")
    @CommandPermission("lemon.command.staffhistory")
    @CommandCompletion("@players")
    fun onStaffHistory(player: Player, uuid: UUID) {
        CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.staffhistory.other")) {
            player.sendMessage(LemonConstants.NO_PERMISSION)
            return
        }

        PunishmentViewMenu(uuid, PunishmentViewType.STAFF_HIST).openMenu(player)
    }

}
