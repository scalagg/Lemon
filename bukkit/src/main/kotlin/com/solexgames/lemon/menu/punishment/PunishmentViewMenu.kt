package com.solexgames.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.QuickAccess.coloredName
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.enums.HistoryViewType
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
class PunishmentViewMenu(
    private val uuid: UUID,
    private val viewType: HistoryViewType,
    private val punishments: List<Punishment>
): Menu() {

    override fun getTitle(player: Player): String {
        val name = CubedCacheUtil.fetchName(uuid)!!
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} ${coloredName(name)}"

        return when (viewType) {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var integer = 10

        PunishmentCategory.values().forEach {
            val totalAmount = punishments.filter { punishment ->
                punishment.category == it
            }.size

            buttons[integer] = ItemBuilder(XMaterial.WHITE_WOOL)
                .name("${getChatColorByIntensity(it)}${it.fancyVersion + "s"}")
                .data(getWoolColorByIntensity(it))
                .amount(totalAmount)
                .addToLore(
                    "${CC.GRAY}Viewing statistics for the",
                    "${CC.GRAY}${it.fancyVersion} category:",
                    "",
                    "${CC.GRAY}Total: ${CC.WHITE}${totalAmount}",
                    "${CC.GRAY}Active: ${CC.YELLOW}${
                        punishments.filter { punishment ->
                            punishment.category == it && punishment.isActive
                        }.size
                    }",
                    "${CC.GRAY}Inactive: ${CC.RED}${
                        punishments.filter { punishment ->
                            punishment.category == it && punishment.isRemoved
                        }.size
                    }",
                    "",
                    "${CC.YELLOW}Click to view more info."
                ).toButton { _, _ ->
                    fetchPunishments(it).whenComplete { list, _ ->
                        PunishmentDetailedViewMenu(
                            uuid, it, viewType, list
                        ).openMenu(player)
                    }
                }

            integer += 2
        }

        for (int in 0..26) {
            buttons.putIfAbsent(int, LemonConstants.EMPTY)
        }

        return buttons
    }

    private fun fetchPunishments(category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return when (viewType) {
            HistoryViewType.TARGET_HIST -> {
                Lemon.instance.punishmentHandler.fetchPunishmentsForTargetOfCategory(uuid, category)
            }
            HistoryViewType.STAFF_HIST -> {
                Lemon.instance.punishmentHandler.fetchPunishmentsByExecutorOfCategory(uuid, category)
            }
        }
    }

    private fun getWoolColorByIntensity(category: PunishmentCategory): Short {
        return when (category.intensity) {
            PunishmentCategoryIntensity.LIGHT -> 1
            PunishmentCategoryIntensity.MEDIUM -> 14
        }
    }

    private fun getChatColorByIntensity(category: PunishmentCategory): ChatColor {
        return when (category.intensity) {
            PunishmentCategoryIntensity.LIGHT -> ChatColor.GOLD
            PunishmentCategoryIntensity.MEDIUM -> ChatColor.RED
        }
    }
}
