package com.solexgames.lemon.command.essentials

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.utility.MinecraftVersion
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Flags
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author puugz
 * @since 10/09/2021 17:27
 */
class RenameCommand : BaseCommand() {

    @CommandAlias("rename")
    @CommandPermission("lemon.command.rename")
    fun onRename(@Flags("itemheld") player: Player, name: String) {
        val combatUpdate = ProtocolLibrary.getProtocolManager().minecraftVersion.isAtLeast(MinecraftVersion.COMBAT_UPDATE)

        val item: ItemStack = if (combatUpdate) {
            player.inventory.itemInMainHand
        } else {
            player.itemInHand
        }

        item.itemMeta.displayName = name

        if (combatUpdate) {
            player.inventory.itemInMainHand = item
        } else {
            player.itemInHand = item
        }

        player.updateInventory()
        player.sendMessage("${CC.GREEN}You've renamed the item in your hand to ${Color.translate(name)}${CC.GREEN}.")
    }
}