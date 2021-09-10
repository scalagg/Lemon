package com.solexgames.lemon.command.essentials

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 10/09/2021 17:49
 */
class CraftCommand : BaseCommand() {

    @CommandAlias("craft|crafting")
    @CommandPermission("lemon.command.craft")
    fun onCraft(player: Player) {
        player.openWorkbench(player.location, true)
    }
}