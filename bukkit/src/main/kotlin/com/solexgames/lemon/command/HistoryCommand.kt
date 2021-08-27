package com.solexgames.lemon.command

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Syntax
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object HistoryCommand: BaseCommand() {

    @Syntax("<player>")
    @CommandAlias("history|c|hist")
    fun onHistory(player: Player, target: String) {
        // TODO: 8/27/2021 load punishments here and open menu
    }

    @Syntax("<player>")
    @CommandAlias("staffhistory|staffhist")
    fun onStaffHistory(player: Player, target: String) {
        // TODO: 8/27/2021 load punishments here and open menu
    }

}
