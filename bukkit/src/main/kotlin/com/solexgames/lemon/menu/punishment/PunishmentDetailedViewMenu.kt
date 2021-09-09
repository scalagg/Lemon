package com.solexgames.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.enums.PunishmentViewType
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.util.CubedCacheUtil
import com.solexgames.lemon.util.SplitUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks.delayed
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
class PunishmentDetailedViewMenu(
    private val uuid: UUID,
    private val category: PunishmentCategory,
    private val viewType: PunishmentViewType,
    private val punishments: List<Punishment>
): PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} ${category.fancyVersion + "s"}"

        return when (viewType) {
            PunishmentViewType.STAFF_HIST -> "Staff $base"
            PunishmentViewType.TARGET_HIST -> base
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return HashMap<Int, Button>().also {
            punishments.forEach { punishment ->
                it[it.size] = PunishmentButton(punishment)
            }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            delayed(1L) {
                PunishmentViewMenu(uuid, viewType).openMenu(player)
            }
        }
    }

    class PunishmentButton(private val punishment: Punishment): Button() {

        override fun getButtonItem(player: Player): ItemStack {
            val lore = ArrayList<String>()

            val statusLore = if (punishment.removed) "${CC.RED}(Removed)" else if (!punishment.hasExpired()) "${CC.GREEN}(Active)" else "${CC.YELLOW}(Expired)"
            val addedBy = punishment.addedBy?.let {
                CubedCacheUtil.fetchName(it)
            } ?: let {
                "${CC.D_RED}Console"
            }

            lore.add(CC.GREEN + "+ " + TimeUtil.formatIntoCalendarString(Date(punishment.addedAt)))

            if (punishment.removed) {
                lore.add(CC.RED + "- " + TimeUtil.formatIntoCalendarString(Date(punishment.removedAt)))
            }
            if (punishment.hasExpired()) {
                lore.add(CC.GOLD + "* " + TimeUtil.formatIntoCalendarString(punishment.expireDate))
            }

            lore.add("")
            lore.add("${CC.SEC}Target: ${CC.PRI}${CubedCacheUtil.fetchName(punishment.target)}")

            if (!punishment.category.instant) {
                lore.add("${CC.SEC}Duration: ${CC.PRI + punishment.getDurationString()}")
            }
            if (!punishment.removed || !punishment.hasExpired()) {
                lore.add("${CC.SEC}Expire Date: ${CC.PRI + punishment.getExpirationString()}")
            }

            lore.add("")
            lore.add("${CC.SEC}Issued By: ${CC.PRI}$addedBy")
            lore.add("${CC.SEC}Issued On: ${CC.PRI}${punishment.addedOn}")
            lore.add("${CC.SEC}Issued Reason: ${CC.PRI}${punishment.addedReason}")
            lore.add("")

            if (punishment.removed) {
                val removedBy = punishment.removedBy?.let {
                    CubedCacheUtil.fetchName(it)
                } ?: let {
                    "${CC.D_RED}Console"
                }

                lore.add("${CC.SEC}Removed By: ${CC.PRI}$removedBy")
                lore.add("${CC.SEC}Removed On: ${CC.PRI}${punishment.removedOn}")
                lore.add("${CC.SEC}Removed Reason: ${CC.PRI}${punishment.removedReason}")
                lore.add("")
            }

            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

            val canRemove: Boolean = lemonPlayer.hasPermission(
                "lemon.punishment.remove." + punishment.category.name.toLowerCase()
            )

            lore.add(if (canRemove) "${CC.GREEN}Click to remove this punishment." else "${CC.RED}You can't remove this punishment.")

            return ItemBuilder(XMaterial.WHITE_WOOL)
                .data((if (!punishment.hasExpired()) 5 else if (punishment.removed) 1 else 14).toShort())
                .name("${CC.D_GRAY}#${SplitUtil.splitUuid(punishment.uuid)} $statusLore")
                .addToLore(lore)
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

            val canRemove: Boolean = lemonPlayer.hasPermission(
                "lemon.punishment.remove." + punishment.category.name.toLowerCase()
            )

            if (!canRemove) {
                player.sendMessage("${CC.RED}Sorry, but you do not have permission to remove ${punishment.category.fancyVersion.toLowerCase()}s.")
                return
            }
        }
    }
}
