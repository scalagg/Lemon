package com.solexgames.lemon.command

import com.solexgames.lemon.menu.PunishmentViewMenu
import com.solexgames.lemon.player.enums.PunishmentViewType
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object HistoryCommand: BaseCommand() {

    @Syntax("<player>")
    @CommandAlias("history|c|hist")
    fun onHistory(player: Player, target: String) {
        // TODO: 8/27/2021 change to uuid param w/ Cubed uuid cache
        val uuid = CubedCacheUtil.fetchUuidByName(target) ?: throw ConditionFailedException("No player matching ${CC.YELLOW}$target${CC.RED} exists.")

        PunishmentViewMenu(
            uuid, PunishmentViewType.TARGET_HIST
        ).openMenu(player)
    }

    @Syntax("<player>")
    @CommandAlias("staffhistory|staffhist")
    fun onStaffHistory(player: Player, target: String) {
        // TODO: 8/27/2021 change to uuid param w/ Cubed uuid cache
        val uuid = CubedCacheUtil.fetchUuidByName(target) ?: throw ConditionFailedException("No player matching ${CC.YELLOW}$target${CC.RED} exists.")

        PunishmentViewMenu(
            uuid, PunishmentViewType.STAFF_HIST
        ).openMenu(player)
    }

}
