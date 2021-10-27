package gg.scala.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.menu.better.BetterConfirmMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.SplitUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
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
    private val uuid: UUID,
    private val category: PunishmentCategory,
    private val viewType: HistoryViewType,
    private val punishments: List<Punishment>
): PaginatedMenu() {

    private val viewingFor = CubedCacheUtil.fetchName(uuid)!!

    override fun getPrePaginatedTitle(player: Player): String {
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} ${category.fancyVersion + "s"}"

        return when (viewType) {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return HashMap<Int, Button>().also {
            punishments.sortedByDescending { it.addedAt }.forEach { punishment ->
                it[it.size] = PunishmentButton(punishment, viewType, viewingFor)
            }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            when (viewType) {
                HistoryViewType.STAFF_HIST -> {
                    Bukkit.dispatchCommand(player, "staffhistory $uuid")
                }
                HistoryViewType.TARGET_HIST -> {
                    Bukkit.dispatchCommand(player, "history $uuid")
                }
            }
        }
    }

    open class PunishmentButton(private val punishment: Punishment, private val viewType: HistoryViewType, private val viewingFor: String): Button() {

        override fun getButtonItem(player: Player): ItemStack {
            val lines = arrayListOf<String>()

            val statusLore = if (punishment.hasExpired) "${CC.YELLOW}(Expired)" else if (!punishment.isRemoved) "${CC.GREEN}(Active)" else "${CC.RED}(Removed)"
            val addedBy = punishment.addedBy?.let {
                CubedCacheUtil.fetchName(it)
            } ?: let {
                LemonConstants.CONSOLE
            }

            lines.add(CC.GREEN + "+ " + TimeUtil.formatIntoCalendarString(Date(punishment.addedAt)))

            if (punishment.hasExpired) {
                lines.add(CC.GRAY + "* " + TimeUtil.formatIntoCalendarString(punishment.expireDate))
            } else if (punishment.isRemoved) {
                lines.add(CC.RED + "- " + TimeUtil.formatIntoCalendarString(Date(punishment.removedAt)))
            }

            lines.add("")
            lines.add("${CC.GRAY}Target: ${CC.WHITE}${CubedCacheUtil.fetchName(punishment.target)}")

            if (!punishment.category.instant) {
                lines.add("${CC.GRAY}Duration: ${CC.WHITE + punishment.durationString}")
            }
            if (punishment.isActive) {
                lines.add("${CC.GRAY}Expire Date: ${CC.WHITE + punishment.expirationString}")
            }

            lines.add("")
            lines.add("${CC.GRAY}Issued By: ${CC.WHITE}$addedBy")
            lines.add("${CC.GRAY}Issued On: ${CC.WHITE}${punishment.addedOn}")
            lines.add("${CC.GRAY}Issued Reason: ${CC.WHITE}${punishment.addedReason}")

            if (punishment.isRemoved) {
                val removedBy = punishment.removedBy?.let {
                    CubedCacheUtil.fetchName(it)
                } ?: let {
                    LemonConstants.CONSOLE
                }

                lines.add("")
                lines.add("${CC.GRAY}Removed By: ${CC.RED}$removedBy")
                lines.add("${CC.GRAY}Removed On: ${CC.RED}${punishment.removedOn}")
                lines.add("${CC.GRAY}Removed Reason: ${CC.RED}${punishment.removedReason}")
            }

            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

            val canRemove: Boolean = lemonPlayer.hasPermission(
                "lemon.punishment.remove." + punishment.category.name.toLowerCase()
            ) && punishment.isActive

            if (punishment.isActive) {
                lines.add("")
                lines.add(if (canRemove) "${CC.YELLOW}Right-Click to remove this punishment!" else "${CC.RED}You can't remove this punishment.")
            }

            return ItemBuilder(XMaterial.WHITE_WOOL)
                .data((if (punishment.hasExpired) 8 else if (!punishment.isRemoved) 5 else 14).toShort())
                .name("$statusLore ${CC.D_GRAY}#${SplitUtil.splitUuid(punishment.uuid)}")
                .addToLore(lines)
                .build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

            if (clickType.name.contains("LEFT"))
            {
                PunishmentSpecificViewMenu(punishment, "${ if (viewType == HistoryViewType.STAFF_HIST) "staffhistory" else "history" } $viewingFor").openMenu(player)
            } else
            {
                val canRemove: Boolean = lemonPlayer.hasPermission(
                    "lemon.punishment.remove." + punishment.category.name.toLowerCase()
                ) && punishment.isActive

                if (!canRemove) return

                InputPrompt()
                    .withText("${CC.SEC}Please enter the ${CC.PRI}Removal Reason${CC.SEC}. ${CC.GRAY}(Type \"cancel\" to exit)")
                    .acceptInput { context, input ->
                        if (input.equals("stop", true) || input.equals("cancel", true)) {
                            context.sendMessage("${CC.RED}You've cancelled the removal operation.")
                            return@acceptInput
                        }

                        context.sendMessage("${CC.SEC}You've set the ${CC.PRI}Removal Reason${CC.SEC} to ${CC.WHITE}$input${CC.SEC}.")

                        val splitUuid = SplitUtil.splitUuid(punishment.uuid)
                        val grantTarget = CubedCacheUtil.fetchName(punishment.target)

                        BetterConfirmMenu(
                            "Punishment Expiration ${Constants.DOUBLE_ARROW_RIGHT} $splitUuid",
                            listOf(
                                "${CC.GRAY}Would you like to expire",
                                "${CC.GRAY}punishment ${CC.WHITE}#$splitUuid${CC.GRAY} from",
                                "${CC.GRAY}player ${grantTarget}?"
                            ), true
                        ) {
                            if (it) {
                                QuickAccess.attemptRemoval(
                                    punishment,
                                    reason = input,
                                    remover = player.uniqueId
                                )

                                player.sendMessage("${CC.SEC}You've removed punishment ${CC.WHITE}#$splitUuid${CC.SEC} from ${CC.PRI}$grantTarget${CC.SEC}.")

                                Tasks.sync {
                                    player.performCommand("${ if (viewType == HistoryViewType.STAFF_HIST) "staffhistory" else "history" } $viewingFor")
                                }
                            } else {
                                player.sendMessage("${CC.RED}You've cancelled the removal operation.")
                            }
                        }.openMenu(player)
                    }.start(player)

                player.closeInventory()
            }
        }
    }
}
