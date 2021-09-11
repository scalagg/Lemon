package com.solexgames.lemon.menu.grant

import com.cryptomorin.xseries.XMaterial
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.menu.better.BetterConfirmMenu
import com.solexgames.lemon.player.enums.HistoryViewType
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.prompt.FuturePrompt
import com.solexgames.lemon.util.CubedCacheUtil
import com.solexgames.lemon.util.SplitUtil
import com.solexgames.lemon.util.quickaccess.coloredName
import com.solexgames.lemon.util.quickaccess.startContinuousPrompt
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit
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
    private val grants: List<Grant>
) : PaginatedMenu() {

    private val viewingFor = CubedCacheUtil.fetchName(uuid)

    override fun getPrePaginatedTitle(player: Player): String {
        val name = CubedCacheUtil.fetchName(uuid)!!
        val base = "Grants ${Constants.DOUBLE_ARROW_RIGHT} ${coloredName(name)}"

        return when (viewType) {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return HashMap<Int, Button>().also {
            grants.sortedByDescending { it.addedAt }.forEach { grant ->
                it[it.size] = GrantButton(grant, viewType, viewingFor!!)
            }
        }
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        return HashMap<Int, Button>().also {
            if (viewType == HistoryViewType.STAFF_HIST && player.uniqueId != uuid) {
                it[4] = ItemBuilder(Material.PISTON_STICKY_BASE)
                    .name("${CC.PRI}Wipe Grants")
                    .addToLore(
                        "${CC.GRAY}Click to wipe all active",
                        "${CC.GRAY}active grants executed",
                        "${CC.GRAY}by ${CC.WHITE}$viewingFor${CC.GRAY}.",
                        "",
                        "${CC.GRAY}Grants will persist in",
                        "${CC.GRAY}their history after the",
                        "${CC.GRAY}wipe.",
                        "",
                        "${CC.YELLOW}Click to start wipe."
                    )
                    .toButton() { clicker, clickType ->
                        if (clicker != null) {
                            if (!clicker.hasPermission("lemon.grants.wipe")) {
                                clicker.sendMessage("${CC.RED}You do not have permission to perform this action!")
                                return@toButton
                            }

                            clicker.sendMessage("${CC.SEC}Starting grant wipe for ${CC.PRI}$viewingFor${CC.SEC}...")

                            Lemon.instance.grantHandler.wipeAllGrantsFor(
                                uuid,
                                clicker
                            ).thenAccept {
                                clicker.sendMessage("${CC.SEC}Finished grant wipe, now updating menu...")

                                Tasks.sync {
                                    Bukkit.dispatchCommand(
                                        player,
                                        "granthistory $viewingFor"
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }

    class GrantButton(private val grant: Grant, private val viewType: HistoryViewType, private val viewingFor: String) : Button() {

        override fun getButtonItem(player: Player): ItemStack {
            val lines = arrayListOf<String>()

            val statusLore = if (grant.hasExpired()) "${CC.YELLOW}(Expired)" else if (!grant.removed) "${CC.GREEN}(Active)" else "${CC.RED}(Removed)"
            val addedBy = grant.addedBy?.let {
                CubedCacheUtil.fetchName(it)
            } ?: let {
                "${CC.D_RED}Console"
            }

            lines.add(CC.GRAY + "+ " + TimeUtil.formatIntoCalendarString(Date(grant.addedAt)))

            if (grant.hasExpired()) {
                lines.add(CC.GOLD + "* " + TimeUtil.formatIntoCalendarString(grant.expireDate))
            } else if (grant.removed) {
                lines.add(CC.RED + "- " + TimeUtil.formatIntoCalendarString(Date(grant.removedAt)))
            }

            lines.add("")
            lines.add("${CC.SEC}Target: ${CC.PRI}${CubedCacheUtil.fetchName(grant.target)}")
            lines.add("${CC.SEC}Duration: ${CC.PRI + grant.getDurationString()}")
            lines.add("${CC.SEC}Expire Date: ${CC.PRI + grant.getExpirationString()}")
            lines.add("")
            lines.add("${CC.SEC}Scopes:")

            grant.scopes.forEach {
                lines.add("${CC.GRAY} - ${CC.RESET}$it")
            }

            lines.add("")
            lines.add("${CC.SEC}Issued By: ${CC.PRI}$addedBy")
            lines.add("${CC.SEC}Issued At: ${CC.PRI}${TimeUtil.formatIntoDateString(Date(grant.addedAt))}")
            lines.add("${CC.SEC}Issued On: ${CC.PRI}${grant.addedOn}")
            lines.add("${CC.SEC}Issued Reason: ${CC.PRI}${grant.addedReason}")
            lines.add("")

            if (grant.removed) {
                val removedBy = grant.removedBy?.let {
                    CubedCacheUtil.fetchName(it)
                } ?: let {
                    "${CC.D_RED}Console"
                }

                lines.add("${CC.SEC}Removed By: ${CC.PRI}$removedBy")
                lines.add("${CC.SEC}Removed At: ${CC.PRI}${TimeUtil.formatIntoDateString(Date(grant.removedAt))}")
                lines.add("${CC.SEC}Removed On: ${CC.PRI}${grant.removedOn}")
                lines.add("${CC.SEC}Removed Reason: ${CC.PRI}${grant.removedReason}")
                lines.add("")
            }

            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

            lines.add(if (grant.canRemove(lemonPlayer)) "${CC.GREEN}Click to remove this grant." else "${CC.RED}You cannot remove this grant.")

            return ItemBuilder(XMaterial.WHITE_WOOL)
                .data((if (grant.hasExpired()) 1 else if (!grant.removed) 5 else 14).toShort())
                .name("${CC.D_GRAY}#${SplitUtil.splitUuid(grant.uuid)} $statusLore")
                .addToLore(lines).build()
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

            if (!clickType.name.contains("RIGHT") || !grant.canRemove(lemonPlayer)) {
                return
            }

            startContinuousPrompt(
                FuturePrompt({ _, input ->
                    val splitUuid = SplitUtil.splitUuid(grant.uuid)
                    val grantTarget = CubedCacheUtil.fetchName(grant.target)

                    BetterConfirmMenu(
                        "Grant Removal ${Constants.DOUBLE_ARROW_RIGHT} $splitUuid",
                        listOf(
                            "${CC.GRAY}Would you like to remove",
                            "${CC.GRAY}grant ${CC.WHITE}#$splitUuid${CC.GRAY} from",
                            "${CC.GRAY}player ${grantTarget}?"
                        ), true
                    ) {
                        if (it) {
                            grant.removed = true
                            grant.removedBy = player.uniqueId
                            grant.removedAt = System.currentTimeMillis()
                            grant.removedOn = Lemon.instance.settings.id
                            grant.removedReason = input

                            player.sendMessage("${CC.SEC}You've removed grant ${CC.WHITE}#$splitUuid${CC.SEC} from ${CC.PRI}$grantTarget${CC.SEC}.")

                            grant.save().thenAccept {
                                Tasks.sync {
                                    Bukkit.dispatchCommand(
                                        player,
                                        "grant${ if (viewType == HistoryViewType.STAFF_HIST) "history" else "s" } $viewingFor"
                                    )
                                }
                            }
                        } else {
                            player.sendMessage("${CC.RED}You've quit the grant removal process.")
                        }
                    }.openMenu(player)

                    return@FuturePrompt null
                }, "Removal Reason"),
                player
            )

            player.closeInventory()
        }
    }
}
