package gg.scala.lemon.menu.better

import gg.scala.lemon.LemonConstants
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
        return hashMapOf<Int, Button>().also {
            if (extraInfo.isEmpty()) {
                it[3] = BetterBooleanButton(this, true, confirm)
                it[5] = BetterBooleanButton(this, false, confirm)
            } else {
                it[2] = BetterBooleanButton(this, true, confirm)
                it[4] = ExtraInfoButton()
                it[6] = BetterBooleanButton(this, false, confirm)
            }

            for (i in 0..8) {
                it.putIfAbsent(i, LemonConstants.EMPTY)
            }
        }
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
