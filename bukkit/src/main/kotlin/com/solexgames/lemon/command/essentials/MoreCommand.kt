package com.solexgames.lemon.command.essentials

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Flags
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 10/09/2021 17:54
 */
class MoreCommand : BaseCommand() {

    @CommandAlias("more|maxitem")
    fun onMore(@Flags("itemheld") player: Player) {
        player.itemInHand.amount = 64
        player.updateInventory()

        player.sendMessage("${CC.GREEN}You've stacked the item in your hand.")
    }

}