package com.solexgames.lemon.command.essentials

import com.solexgames.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 10/09/2021 17:30
 */
class ClearCommand : BaseCommand() {

    @CommandAlias("clear|clearinv|ci|clearinventory")
    @CommandPermission("lemon.command.clear")
    @CommandCompletion("@players")
    fun execute(player: Player, @Optional target: OnlinePlayer?) {
        val name: String

        if (target != null) {
            name = coloredName(target.player)!!
            target.player.clearInventory()
        } else {
            name = coloredName(player)!!
            player.clearInventory()
        }

        player.sendMessage("${CC.SEC}You've cleared $name${CC.SEC}'s inventory.")
    }

    private fun Player.clearInventory() {
        this.inventory.clear()
        this.updateInventory()
    }
}
