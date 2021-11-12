package gg.scala.lemon.player.color

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 11/12/2021
 */
class PlayerColorMenu : PaginatedMenu()
{
    companion object
    {
        @JvmStatic
        val SLOTS = listOf(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25
        )
    }

    init
    {
        placeholdBorders = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 36
    override fun getAllPagesButtonSlots(): List<Int> = SLOTS

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            for (colorPair in PlayerColorHandler.colorPairs)
            {
                it[it.size] = PlayerColorButton(colorPair)
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player): String = "Choose a Color"

    inner class PlayerColorButton(
        private val playerColor: PlayerColor
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)!!
            val metadata = lemonPlayer.getMetadata("color")

            val hasEquipped = if (metadata != null)
                metadata.asString() == playerColor.name else false

            return ItemBuilder(XMaterial.LEATHER_CHESTPLATE)
                .color(playerColor.bukkitColor)
                .name("${playerColor.chatColor}${CC.BOLD}${playerColor.name}")
                .addToLore(
                    "${CC.SEC}Display: ${
                        QuickAccess.realRank(player).prefix
                    }${playerColor.chatColor}${player.name}",
                    "",
                    if (!hasEquipped) "${CC.GREEN}Click to equip!" else "${CC.GOLD}Click to un-equip!"
                )
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)!!
            val metadata = lemonPlayer.getMetadata("color")

            val hasEquipped = if (metadata != null)
                metadata.asString() == playerColor.name else false

            if (hasEquipped)
            {
                lemonPlayer remove "color"
            } else
            {
                lemonPlayer.updateOrAddMetadata(
                    "color", Metadata(playerColor.name)
                )
            }

            player.sendMessage(
                "${CC.SEC}You've ${
                    if (hasEquipped) "un" else ""
                }equipped ${playerColor.chatColor}${CC.BOLD}${playerColor.name}${CC.SEC}!"
            )
        }
    }
}
