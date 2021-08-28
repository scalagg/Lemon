package com.solexgames.lemon.command

import com.solexgames.lemon.menu.PunishmentViewMenu
import com.solexgames.lemon.player.enums.PunishmentViewType
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
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
    fun onHistory(player: Player, uuid: UUID) {
        CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")
        PunishmentViewMenu(uuid, PunishmentViewType.TARGET_HIST).openMenu(player)
    }

    @Syntax("<player>")
    @CommandAlias("staffhistory|staffhist")
    fun onStaffHistory(player: Player, uuid: UUID) {
        CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")
        PunishmentViewMenu(uuid, PunishmentViewType.STAFF_HIST).openMenu(player)
    }

}
