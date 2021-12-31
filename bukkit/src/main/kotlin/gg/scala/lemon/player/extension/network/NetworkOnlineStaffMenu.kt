package gg.scala.lemon.player.extension.network

import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.menu.staff.StaffListMenu
import gg.scala.lemon.player.FundamentalLemonPlayer
import gg.scala.lemon.player.extension.PlayerCachingExtension
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author GrowlyX
 * @since 11/22/2021
 */
class NetworkOnlineStaffMenu : PaginatedMenu()
{
    private lateinit var onlineStaff: List<FundamentalLemonPlayer>

    init
    {
        placeholdBorders = true
        async = true
    }

    override fun asyncLoadResources(player: Player, callback: (Boolean) -> Unit)
    {
        PlayerCachingExtension.controller.loadAll(DataStoreStorageType.REDIS)
            .thenApply {
                val staffList = mutableListOf<FundamentalLemonPlayer>()

                it.values.forEach { staff ->
                    if (isStaffRank(staff.currentRank))
                    {
                        staffList.add(staff)
                    }
                }

                return@thenApply staffList
            }
            .thenAccept {
                onlineStaff = it
                callback.invoke(true)
            }
    }

    private fun isStaffRank(uuid: UUID): Boolean
    {
        val rank = RankHandler.findRank(uuid)
            ?: return false

        return rank.getCompoundedPermissions()
            .contains("lemon.staff")
    }

    override fun getAllPagesButtonSlots() = StaffListMenu.SLOTS
    override fun getAllPagesButtons(player: Player) = mutableMapOf<Int, Button>().also {
        for (staff in onlineStaff)
        {
            it[it.size] = OnlineStaffButton(staff)
        }
    }

    override fun size(buttons: Map<Int, Button>) = 45

    override fun getMaxItemsPerPage(player: Player) = StaffListMenu.SLOTS.size

    override fun getPrePaginatedTitle(player: Player) = "Online Staff"

    inner class OnlineStaffButton(
        private val cached: FundamentalLemonPlayer
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            val description = mutableListOf<String>()
            description.add("${CC.GRAY}Server: ${CC.WHITE}${cached.currentServer}")
            description.add("")
            description.add("${CC.YELLOW}Click to jump.")

            return ItemBuilder(Material.SKULL_ITEM)
                .data(3)
                .name(cached.currentDisplayName)
                .owner(cached.username)
                .setLore(description)
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            player.chat("/join ${cached.currentServer}")
            player.closeInventory()
        }
    }
}
