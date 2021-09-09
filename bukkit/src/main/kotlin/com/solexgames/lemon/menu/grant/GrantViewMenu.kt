package com.solexgames.lemon.menu.grant

import com.cryptomorin.xseries.XMaterial
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.menu.punishment.PunishmentDetailedViewMenu
import com.solexgames.lemon.player.enums.HistoryViewType
import com.solexgames.lemon.player.grant.Grant
import com.solexgames.lemon.util.CubedCacheUtil
import com.solexgames.lemon.util.SplitUtil
import com.solexgames.lemon.util.quickaccess.coloredName
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player
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

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return HashMap<Int, Button>().also {
            grants.forEach { grant ->
                it[it.size] = GrantButton(grant)
            }
        }
    }

    override fun getPrePaginatedTitle(player: Player): String {
        val name = CubedCacheUtil.fetchName(uuid)!!
        val base = "Grants ${Constants.DOUBLE_ARROW_RIGHT} ${coloredName(name)}"

        return when (viewType) {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    class GrantButton(private val grant: Grant) : Button() {

        override fun getButtonItem(player: Player): ItemStack {
            val lines = arrayListOf<String>()

            val statusLore = if (grant.removed) "${CC.RED}(Removed)" else if (!grant.hasExpired()) "${CC.GREEN}(Active)" else "${CC.YELLOW}(Expired)"
            val addedBy = grant.addedBy?.let {
                CubedCacheUtil.fetchName(it)
            } ?: let {
                "${CC.D_RED}Console"
            }

            lines.add(CC.GRAY + "+ " + TimeUtil.formatIntoCalendarString(Date(grant.addedAt)))

            if (grant.removed) {
                lines.add(CC.RED + "- " + TimeUtil.formatIntoCalendarString(Date(grant.removedAt)))
            }
            if (grant.hasExpired()) {
                lines.add(CC.GOLD + "* " + TimeUtil.formatIntoCalendarString(grant.expireDate))
            }

            lines.add("")
            lines.add("${CC.SEC}Target: ${CC.PRI}${CubedCacheUtil.fetchName(grant.target)}")
            lines.add("${CC.SEC}Duration: ${CC.PRI + grant.getDurationString()}")
            lines.add("${CC.SEC}Expire Date: ${CC.PRI + grant.getExpirationString()}")
            lines.add("")
            lines.add("${CC.SEC}Issued By: ${CC.PRI}$addedBy")
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
                lines.add("${CC.SEC}Removed On: ${CC.PRI}${grant.removedOn}")
                lines.add("${CC.SEC}Removed Reason: ${CC.PRI}${grant.removedReason}")
                lines.add("")
            }

            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
            val canRemove: Boolean = lemonPlayer.activeGrant!!.getRank().weight >= grant.getRank().weight

            lines.add(if (canRemove) "${CC.GREEN}Click to remove this punishment." else "${CC.RED}You can't remove this punishment.")

            return ItemBuilder(XMaterial.WHITE_WOOL)
                .data((if (!grant.hasExpired()) 5 else if (grant.removed) 1 else 14).toShort())
                .name("${CC.D_GRAY}#${SplitUtil.splitUuid(grant.uuid)} $statusLore")
                .addToLore(lines).build()
        }
    }
}
