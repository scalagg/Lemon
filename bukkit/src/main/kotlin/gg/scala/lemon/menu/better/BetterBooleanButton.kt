package gg.scala.lemon.menu.better

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
class BetterBooleanButton(
    private val parent: ConfirmMenu,
    private val value: Boolean,
    private val confirm: Boolean = false
) : Button() {

    override fun getName(player: Player): String {
        return if (value) {
            if (confirm) {
                "${ChatColor.GREEN}Confirm"
            } else {
                "${ChatColor.GREEN}Yes"
            }
        } else {
            if (confirm) {
                "${ChatColor.RED}Cancel"
            } else {
                "${ChatColor.RED}No"
            }
        }
    }

    override fun getDescription(player: Player): List<String> {
        return listOf(
            if (value) {
                if (confirm) {
                    "${ChatColor.GRAY}I want to proceed with this action."
                } else {
                    "${ChatColor.GRAY}I agree with this action."
                }
            } else {
                if (confirm) {
                    "${ChatColor.GRAY}I want to cancel this procedure"
                } else {
                    "${ChatColor.GRAY}I do not agree with this action."
                }
            }
        )
    }

    override fun getDamageValue(player: Player): Byte {
        return (if (this.value) 5 else 14).toByte()
    }

    override fun getMaterial(player: Player): Material {
        return Material.CARPET
    }

    override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
        if (parent.called) {
            return
        }

        if (value) {
            playSuccess(player)
        } else {
            playFail(player)
        }

        player.closeInventory()

        parent.invokeCallback(value)
    }

}
