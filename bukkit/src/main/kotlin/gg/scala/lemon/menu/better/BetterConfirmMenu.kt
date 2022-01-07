package gg.scala.lemon.menu.better

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.util.CC
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
open class BetterConfirmMenu(
    private val title: String = "Are you sure?",
    private val extraInfo: List<String> = listOf("${CC.GRAY}Do you really want to do this?"),
    private val confirm: Boolean = true,
    callback: (Boolean) -> Unit
) : ConfirmMenu(title, extraInfo, confirm, callback) {

    init {
        placeholder = true
    }

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
