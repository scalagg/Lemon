package gg.scala.lemon.menu.staff

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 10/10/2021
 */
class StaffListMenu : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val SLOTS = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
        )
    }

    init
    {
        placeholdBorders = true
    }

    override fun getPrePaginatedTitle(player: Player): String = "Staff List"

    override fun getAllPagesButtonSlots(): List<Int> = SLOTS
    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return hashMapOf<Int, Button>().also {
            Bukkit.getOnlinePlayers()
                .filter { it.hasPermission("lemon.staff") }
                .forEach { target ->
                    it[it.size] = StaffButton(target)
                }
        }
    }

    override fun getMaxItemsPerPage(player: Player) = 21

    inner class StaffButton(
        private val target: Player
    ) : Button() {

        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder(XMaterial.SKELETON_SKULL)
                .name(
                    QuickAccess.coloredName(target)
                )
                .owner(
                    target.name
                )
                .addToLore(
                    "${CC.GRAY}Vanish: ${
                        if (target.hasMetadata("vanished"))
                        {
                            "${CC.GREEN}Yes"
                        } else
                        {
                            "${CC.RED}No"
                        }
                    }",
                    "${CC.GRAY}Mod Mode: ${
                        if (target.hasMetadata("mod-mode"))
                        {
                            "${CC.GREEN}Yes"
                        } else
                        {
                            "${CC.RED}No"
                        }
                    }",
                    "",
                    "${CC.YELLOW}Click to teleport."
                )
                .data(3).build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            player.performCommand("teleport ${target.name}")
            player.closeInventory()
        }
    }

}
