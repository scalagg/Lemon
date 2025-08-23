package gg.scala.lemon.menu.rank

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.menu.rank.attribute.RankAttributeEditorMenu
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.CallbackInputPrompt
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Class created on 1/1/2025

 * @author Max C.
 * @project lemon
 * @website https://solo.to/redis
 */
class RankManagementMenu : PaginatedMenu()
{
    init
    {
        placeholdBorders = true
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getMaxItemsPerPage(player: Player) = 14
    override fun getAllPagesButtonSlots() = (10..16) + (19..25)

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also { map ->
            RankHandler.sorted.forEach { rank ->
                map[map.size] = RankButton(rank)
            }
        }
    }

    override fun getGlobalButtons(player: Player) =
        mutableMapOf<Int, Button>()
            .apply {
                this[4] = ItemBuilder
                    .of(Material.EMERALD)
                    .name("${CC.GREEN}Create a Rank")
                    .addToLore(
                        "${CC.GRAY}Click here to create",
                        "${CC.GRAY}a new global rank.",
                        "",
                        "${CC.GREEN}Click to create a new rank"
                    )
                    .toButton { _, _ ->
                        Button.playSuccess(player)
                        CallbackInputPrompt("${CC.GREEN}Enter the name you want the new rank to have:") { name ->
                                val existing = RankHandler.findRank(name)

                                if (existing != null) {
                                    throw ConditionFailedException("A rank with the name matching ${CC.YELLOW}$name${CC.RED} already exists.")
                                }

                                val rank = Rank(UUID.randomUUID(), name)

                                rank.saveAndPushUpdatesGlobally().thenAccept {
                                    player.sendMessage("${CC.SEC}You've created the ${CC.PRI}${rank.getColoredName()}${CC.SEC} rank.")
                                }
                                Button.playSuccess(player)
                                openMenu(player)
                            }.start(player)
                    }
            }

    override fun getPrePaginatedTitle(player: Player): String
    {
        return "Managing Ranks..."
    }

    internal class RankButton(val rank: Rank) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            return ItemBuilder
                .of(
                    Material.WOOL
                )
                .data(
                    (ColorUtil.CHAT_COLOR_TO_WOOL_DATA[
                        ChatColor.getByChar(rank.color[1]) ?: ChatColor.WHITE
                    ]?.toByte() ?: 1).toShort()
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
                    "${CC.GREEN}Left-Click to edit the properties of this rank!"
                )
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) =
            RankAttributeEditorMenu(rank).openMenu(player)

    }
}