package gg.scala.lemon.menu.grant

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.menu.punishment.PunishmentDetailedViewMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.SplitUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.text.TextSplitter
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author GrowlyX
 * @since 9/9/2021
 */
class GrantViewMenu(
    private val uuid: UUID,
    private val viewType: HistoryViewType,
    private val grants: List<Grant>,
    val colored: String
) : PaginatedMenu()
{

    private val viewingFor = CubedCacheUtil.fetchName(uuid)

    init
    {
        placeholdBorders = true
    }

    override fun getMaxItemsPerPage(player: Player) = PunishmentDetailedViewMenu.SLOTS.size - 1
    override fun getAllPagesButtonSlots() = PunishmentDetailedViewMenu.SLOTS

    override fun size(buttons: Map<Int, Button>) = 36

    override fun getPrePaginatedTitle(player: Player): String
    {
        val base = "Applicable grants ${Constants.DOUBLE_ARROW_RIGHT} $colored${CC.D_GRAY}"

        return when (viewType)
        {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button>
    {
        return HashMap<Int, Button>().also {
            grants.sortedByDescending { it.addedAt }.forEach { grant ->
                it[it.size] = GrantButton(grant, viewType, viewingFor!!, colored)
            }
        }
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button>
    {
        return HashMap<Int, Button>().also {
            if (viewType == HistoryViewType.STAFF_HIST && player.uniqueId != uuid)
            {
                it[4] = ItemBuilder(XMaterial.STICKY_PISTON)
                    .name("${CC.B_GREEN}Invalidate Grants")
                    .addToLore(
                        "${CC.WHITE}Click to invalidate all active",
                        "${CC.WHITE}active grants executed",
                        "${CC.WHITE}by ${CC.WHITE}$colored${CC.WHITE}.",
                        "",
                        "${CC.WHITE}Grants will persist in",
                        "${CC.WHITE}their history after the",
                        "${CC.WHITE}invalidation.",
                        "",
                        "${CC.YELLOW}Shift-click to start invalidation."
                    )
                    .toButton { clicker, type ->
                        if (clicker != null && type != null && type.isShiftClick)
                        {
                            if (!clicker.hasPermission("lemon.grants.wipe"))
                            {
                                clicker.sendMessage("${CC.RED}You do not have permission to perform this action!")
                                return@toButton
                            }

                            clicker.sendMessage("${CC.GRAY}Starting grant wipe for ${CC.WHITE}$viewingFor${CC.GRAY}...")

                            GrantHandler.invalidateAllGrantsBy(
                                uuid, clicker
                            ).thenAccept {
                                clicker.sendMessage("${CC.GRAY}Finished grant wipe, now updating menu...")

                                Tasks.sync {
                                    player.performCommand("grantstaffhistory $viewingFor")
                                }
                            }
                        }
                    }
            }
        }
    }

    internal class GrantButton(
        private val grant: Grant,
        private val viewType: HistoryViewType,
        private val viewingFor: String,
        private val colored: String
    ) : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            val lines = arrayListOf<String>()

            val statusLore =
                if (grant.hasExpired) "${CC.YELLOW}(Expired)" else if (!grant.isRemoved) "${CC.GREEN}(Active)" else "${CC.RED}(Removed)"
            val addedBy = grant.addedBy?.let {
                CubedCacheUtil.fetchName(it)
            } ?: let {
                LemonConstants.CONSOLE
            }

            lines.add(CC.GREEN + "+ " + TimeUtil.formatIntoCalendarString(Date(grant.addedAt)))

            if (grant.hasExpired)
            {
                lines.add(CC.GRAY + "* " + TimeUtil.formatIntoCalendarString(grant.expireDate))
            } else if (grant.isRemoved)
            {
                lines.add(CC.RED + "- " + TimeUtil.formatIntoCalendarString(Date(grant.removedAt)))
            }

            lines.add("")
            lines.add("${CC.GRAY}Target: ${CC.WHITE}${coloredName(grant.target) ?: CubedCacheUtil.fetchName(grant.target)}")
            lines.add("${CC.GRAY}Rank: ${CC.WHITE}${grant.getRank().getColoredName()}")
            lines.add("${CC.GRAY}Duration: ${CC.WHITE + grant.durationString}")

            if (grant.isActive && !grant.isPermanent)
            {
                lines.add("")
                lines.add("${CC.GRAY}Expires In: ${CC.WHITE + grant.durationFromNowStringRaw}")
                lines.add("${CC.GRAY}Expires On: ${CC.WHITE + grant.expirationString}")
            }

            lines.add("")
            lines.add("${CC.GRAY}Scopes:")

            grant.scopes.forEach {
                lines.add("${CC.GRAY} - ${CC.GREEN}$it")
            }

            lines.add("")

            if (player.hasPermission("lemon.history.grant.view-issuer"))
            {
                lines.add("${CC.GRAY}Issued By: ${CC.WHITE}$addedBy")
            }

            lines.add("${CC.GRAY}Issued On: ${CC.WHITE}${grant.addedOn}")

            lines.addAll(
                TextSplitter.split(
                    text = "${CC.GRAY}Issued Reason: ${CC.WHITE}${grant.addedReason}",
                    linePrefix = CC.WHITE
                )
            )

            if (grant.isRemoved)
            {
                val removedBy = grant.removedBy?.let {
                    CubedCacheUtil.fetchName(it)
                } ?: let {
                    LemonConstants.CONSOLE
                }

                lines.add("")

                if (player.hasPermission("lemon.history.grant.view-issuer"))
                {
                    lines.add("${CC.GRAY}Removed By: ${CC.RED}$removedBy")
                }

                lines.add("${CC.GRAY}Removed On: ${CC.RED}${grant.removedOn}")

                lines.addAll(
                    TextSplitter.split(
                        text = "${CC.GRAY}Removed Reason: ${CC.RED}${grant.removedReason}",
                        linePrefix = CC.RED
                    )
                )
            }

            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

            if (grant.isActive)
            {
                lines.add("")
                lines.add(if (grant.canRemove(lemonPlayer)) "${CC.YELLOW}Click to remove this grant!" else "${CC.RED}You can't remove this grant.")
            }

            return ItemBuilder(XMaterial.WHITE_WOOL)
                .data((if (grant.hasExpired) 7 else if (!grant.isRemoved) if (grant.isCustomScope()) 1 else if (grant.isPermanent) 5 else 13 else 14).toShort())
                .name("$statusLore ${CC.D_GRAY}#${SplitUtil.splitUuid(grant.uuid)}")
                .addToLore(lines).build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView)
        {
            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null) ?: return

            if (!grant.canRemove(lemonPlayer)) return

            InputPrompt()
                .withText("${CC.SEC}Please enter the ${CC.WHITE}Removal Reason${CC.SEC}. ${CC.GRAY}(Type \"cancel\" to exit)")
                .acceptInput { context, input ->
                    if (input.equals("stop", true) || input.equals("cancel", true))
                    {
                        context.sendMessage("${CC.RED}You've cancelled the removal operation.")
                        return@acceptInput
                    }

                    context.sendMessage("${CC.SEC}You've set the ${CC.WHITE}Removal Reason${CC.SEC} to ${CC.WHITE}$input${CC.SEC}.")

                    val splitUuid = SplitUtil.splitUuid(grant.uuid)

                    ConfirmMenu(
                        "Grant Removal ${Constants.DOUBLE_ARROW_RIGHT} $splitUuid",
                        listOf(
                            "${CC.GRAY}Would you like to remove",
                            "${CC.GRAY}grant ${CC.WHITE}#$splitUuid${CC.GRAY} from",
                            "${CC.GRAY}player $colored${CC.GRAY}?"
                        ), true
                    ) {
                        if (it)
                        {
                            grant.removedBy = player.uniqueId
                            grant.removedAt = System.currentTimeMillis()
                            grant.removedOn = Lemon.instance.settings.id
                            grant.removedReason = input

                            player.sendMessage("${CC.SEC}You've removed 1 grant from ${CC.WHITE}$colored${CC.SEC}.")

                            grant.save().thenAccept {
                                Tasks.sync {
                                    player.performCommand("grant${if (viewType == HistoryViewType.STAFF_HIST) "staffhistory" else "s"} $viewingFor")
                                }
                            }
                        } else
                        {
                            player.sendMessage("${CC.RED}You've cancelled the removal operation.")
                        }
                    }.openMenu(player)
                }.start(player)

            player.closeInventory()
        }
    }
}
