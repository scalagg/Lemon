package gg.scala.lemon.player.extension.network

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.util.QuickAccess.username
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 11/22/2021
 */
class NetworkOnlineStaffMenu : PaginatedMenu()
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

        autoUpdate = true
        autoUpdateInterval = 1000L
    }

    override fun getAllPagesButtonSlots() = SLOTS

    override fun getAllPagesButtons(player: Player) = mutableMapOf<Int, Button>()
        .also {
            val sorted = NetworkOnlineStaffUpdates.staffMembers
                .sortedByDescending { member ->
                    RankHandler.findRank(member.rankId)?.weight ?: 0
                }

            for (staff in sorted)
            {
                it[it.size] = OnlineStaffButton(staff)
            }
        }

    override fun size(buttons: Map<Int, Button>) = 45
    override fun getMaxItemsPerPage(player: Player) = SLOTS.size

    override fun getPrePaginatedTitle(player: Player) = "Online Staff"

    inner class OnlineStaffButton(
        private val staffMember: NetworkOnlineStaffUpdates.StaffMember
    ) : Button()
    {
        private val username = staffMember.uniqueId.username()

        override fun getButtonItem(player: Player): ItemStack
        {
            val rank = RankHandler.findRank(staffMember.rankId)?.color ?: CC.GRAY

            val rankData = RankHandler
                .findRank(staffMember.rankId)
                ?: RankHandler.getDefaultRank()

            val description = mutableListOf<String>()
            description.add("${CC.WHITE}Rank: ${CC.WHITE}${rankData.prefix}")
            description.add("${CC.WHITE}Server: ${CC.YELLOW}${staffMember.server}")
            description.add("")
            description.add("${CC.GREEN}Click to jump.")

            return ItemBuilder(XMaterial.SKELETON_SKULL)
                .name("$rank$username")
                .owner(username)
                .setLore(description)
                .data(3).build()
        }

        override fun clicked(
            player: Player, slot: Int,
            clickType: ClickType, view: InventoryView
        )
        {
            player.chat("/jump $username")
            player.closeInventory()
        }
    }
}
