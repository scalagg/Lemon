package com.solexgames.lemon.menu

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
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.*


/**
 * @author GrowlyX
 * @since 8/27/2021
 */
class PunishmentDetailedViewMenu(
    private var uuid: UUID,
    private var category: PunishmentCategory,
    private var viewType: PunishmentViewType,
    private var punishments: List<Punishment>
): PaginatedMenu() {

    companion object {
        class PunishmentButton(private val punishment: Punishment): Button() {

            override fun getButtonItem(player: Player): ItemStack {
                val lore = mutableListOf<String>()

                val statusLore = if (punishment.removed) CC.RED + "(Removed)" else if (!punishment.hasExpired()) CC.GREEN + "(Active)" else CC.GOLD + "(Expired)"
                val issuer = if (punishment.addedBy == null) CC.D_RED + "Console" else CubedCacheUtil.fetchNameByUuid(punishment.addedBy)

                lore.add(CC.GRAY + TimeUtil.formatIntoCalendarString(Date(punishment.addedAt)))

                lore.add("  ")
                lore.add("${CC.SEC}Issuer: ${CC.PRI + issuer}")
                lore.add("${CC.SEC}Target: ${CC.PRI + CubedCacheUtil.fetchNameByUuid(punishment.target)}")

                if (!punishment.category.instant) {
                    lore.add("${CC.SEC}Duration: ${CC.PRI + punishment.getDurationString()}")
                }

                lore.add("  ")
                lore.add("${CC.SEC}Reason: ${CC.PRI + punishment.addedReason}")
                lore.add("${CC.SEC}Server: ${CC.PRI + punishment.addedOn}")
                lore.add("  ")
                lore.add("${CC.SEC}Expire Date: ${CC.PRI + punishment.getExpirationString()}")
                lore.add("  ")

                if (punishment.removed) {
                    lore.add(
                        "${CC.SEC}Remover: ${ CC.PRI + if (punishment.removedBy != null) CubedCacheUtil.fetchNameByUuid(
                            punishment.removedBy!!
                        ) else CC.D_RED + "Console"}"
                    )
                    lore.add("${CC.SEC}Removal Reason: ${CC.PRI + punishment.removedReason}")
                    lore.add("  ")
                }

                val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

                val canRemove: Boolean = lemonPlayer.hasPermission(
                    "lemon.punishment.remove." + punishment.category.name.lowercase()
                )

                lore.add(if (canRemove) CC.GREEN + "[Click to remove]" else CC.RED + "[You cannot remove this]")

                return ItemBuilder(XMaterial.LIME_WOOL)
                    .data((if (!punishment.hasExpired()) 5 else if (punishment.removed) 1 else 14).toShort())
                    .name("${CC.D_GRAY}#${SplitUtil.splitUuid(punishment.uuid)} $statusLore")
                    .addToLore(*lore.toTypedArray()).build()
            }

            override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
                // TODO: 8/27/2021 handle un-punish things
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player): String {
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} ${category.fancyVersion + "s"}"

        return when (viewType) {
            PunishmentViewType.STAFF_HIST -> "Staff $base"
            PunishmentViewType.TARGET_HIST -> base
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var integer = 0

        punishments.forEach {
            buttons[integer++] = PunishmentButton(it)
        }

        return buttons
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            delayed(1L) {
                PunishmentViewMenu(uuid, viewType).openMenu(player)
            }
        }
    }
}
