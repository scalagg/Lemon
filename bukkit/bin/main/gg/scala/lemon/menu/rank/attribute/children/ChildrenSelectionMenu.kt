package gg.scala.lemon.menu.rank.attribute.children

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.menu.rank.attribute.RankAttributeEditorMenu
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * Class created on 1/2/2025

 * @author Max C.
 * @project lemon
 * @website https://solo.to/redis
 */
class ChildrenSelectionMenu(val rank: Rank) : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
        updateAfterClick = true
        applyHeader = true
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
    override fun getPrePaginatedTitle(player: Player): String = "Select Rank's Children"
    override fun getAllPagesButtonSlots() = (10..16) + (19..25)

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { buttons ->
            RankHandler.sorted.forEach {
                buttons[buttons.size] = RankSelectButton(it, rank)
            }
        }
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            4 to ItemBuilder.of(XMaterial.ARROW)
                .name("${CC.RED}Go Back")
                .toButton { _, _ ->
                    RankAttributeEditorMenu(rank).openMenu(player)
                }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose) {
            Tasks.sync {
                RankAttributeEditorMenu(rank).openMenu(player)
            }
        }
    }

    internal class RankSelectButton(val rank: Rank, private val toModify: Rank) : Button()
    {
        val contains = toModify.children.contains(rank.uuid)

        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder
                .of(
                    if (toModify.children.contains(rank.uuid)) XMaterial.GREEN_WOOL else XMaterial.RED_WOOL
                )
                .name(rank.getColoredName(true))
                .addToLore(
                    "${CC.GRAY}Name: ${CC.WHITE}${rank.name}",
                    "${CC.GRAY}Weight: ${CC.WHITE}${
                        Numbers.format(rank.weight)
                    }",
                    "",
                    "${CC.GREEN}Metadata:",
                    "${CC.GRAY}Display Name: ${
                        if (rank.displayName == null) "${CC.RED}None" else "${CC.WHITE}${rank.displayName}"
                    }",
                    "${CC.GRAY}Prefix: ${CC.WHITE}${
                        QuickAccess.replaceEmpty(rank.prefix)
                    }",
                    "${CC.GRAY}Suffix: ${CC.WHITE}${
                        QuickAccess.replaceEmpty(rank.suffix)
                    }",
                    "${CC.GRAY}Visible: ${
                        if (rank.visible) "${CC.GREEN}Yes" else "${CC.RED}No"
                    }",
                    " ",
                    "${CC.GREEN}Extensions:",
                    "${CC.GRAY}Permissions: ${CC.WHITE}${rank.permissions.size}",
                    "${CC.GRAY}Children: ${CC.WHITE}${rank.children.size}",
                    "${CC.GRAY}Scopes: ${CC.WHITE}${rank.scopes().size}",
                    " ",
                    if (!toModify.children.contains(rank.identifier))
                        "${CC.GREEN}Left-Click to add this rank as a child!" else "${CC.RED}Left-Click to remove this rank from the children list"
                )
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            if (toModify.children.contains(rank.identifier))
            {
                toModify.children.remove(rank.identifier)
            } else
            {
                toModify.children.add(rank.identifier)
            }

            toModify.saveAndPushUpdatesGlobally()
        }
    }
}