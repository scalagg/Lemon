package com.solexgames.lemon.command.essentials

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Flags
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 10/09/2021 17:27
 */
class RenameCommand : BaseCommand() {

    @CommandAlias("rename")
    @CommandPermission("lemon.command.rename")
    fun onRename(@Flags("itemheld") player: Player, name: String) {
        val item = player.itemInHand.clone()

        item.itemMeta.displayName = CC.WHITE + name

        player.itemInHand = item
        player.updateInventory()

        player.sendMessage("${CC.GREEN}You've renamed the item in your hand to ${CC.WHITE}${Color.translate(name)}${CC.GREEN}.")
    }
}