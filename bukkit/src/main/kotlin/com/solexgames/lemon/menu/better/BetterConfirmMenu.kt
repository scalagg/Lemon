package com.solexgames.lemon.menu.better

import com.solexgames.lemon.LemonConstants
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.buttons.TexturedHeadButton
import net.evilblock.cubed.menu.menus.ConfirmMenu
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
open class BetterConfirmMenu(
    private val title: String = "Are you sure?",
    private val extraInfo: List<String> = emptyList(),
    private val confirm: Boolean = true,
    callback: (Boolean) -> Unit
) : ConfirmMenu(title, extraInfo, confirm, callback) {

    override fun getTitle(player: Player): String {
        return title
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = HashMap<Int, Button>()

        if (extraInfo.isEmpty()) {
            buttons[3] = BetterBooleanButton(this, true, confirm)
            buttons[5] = BetterBooleanButton(this, false, confirm)
        } else {
            buttons[2] = BetterBooleanButton(this, true, confirm)
            buttons[6] = BetterBooleanButton(this, false, confirm)
            buttons[4] = ExtraInfoButton()
        }

        for (i in 0..8) {
            buttons.putIfAbsent(i, LemonConstants.EMPTY)
        }

        return buttons
    }

    private inner class ExtraInfoButton : Button() {
        override fun getName(player: Player): String {
            return "${ChatColor.RED}Are you sure?"
        }

        override fun getDescription(player: Player): List<String> {
            return extraInfo
        }

        override fun getMaterial(player: Player): Material {
            return Material.SIGN
        }
    }

}
