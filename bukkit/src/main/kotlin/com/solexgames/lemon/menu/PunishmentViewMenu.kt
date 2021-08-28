package com.solexgames.lemon.menu

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.player.enums.PunishmentViewType
import com.solexgames.lemon.player.punishment.Punishment
import com.solexgames.lemon.player.punishment.category.PunishmentCategory
import com.solexgames.lemon.player.punishment.category.PunishmentCategoryIntensity
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.apache.commons.lang.StringUtils
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
class PunishmentViewMenu(
    private var uuid: UUID,
    private var viewType: PunishmentViewType
): Menu() {

    override fun getTitle(player: Player): String {
        val name = CubedCacheUtil.fetchName(uuid)
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} $name"

        return when (viewType) {
            PunishmentViewType.STAFF_HIST -> "Staff $base"
            PunishmentViewType.TARGET_HIST -> base
        }
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = mutableMapOf<Int, Button>()
        var integer = 0

        PunishmentCategory.values().forEach {
            buttons[integer++] = ItemBuilder.of(Material.WOOL)
                .name("${getChatColorByIntensity(it)}${it.fancyVersion + "s"}")
                .data(getWoolColorByIntensity(it))
                .addToLore(
                    "${CC.GRAY}Click to view punishments",
                    "${CC.GRAY}for this category."
                ).toButton { _, _ ->
                    this.fetchPunishments(it).whenComplete { list, _ ->
                        PunishmentDetailedViewMenu(
                            uuid, it, viewType, list
                        ).openMenu(player)
                    }
                }
        }

        for (int in LemonConstants.SINGLE_ROW) {
            buttons.putIfAbsent(int, LemonConstants.EMPTY)
        }

        return buttons
    }

    private fun fetchPunishments(category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return when (viewType) {
            PunishmentViewType.TARGET_HIST -> {
                Lemon.instance.punishmentHandler.fetchPunishmentsForTargetOfCategory(uuid, category)
            }
            PunishmentViewType.STAFF_HIST -> {
                Lemon.instance.punishmentHandler.fetchPunishmentsByExecutorOfCategory(uuid, category)
            }
        }
    }

    private fun getWoolColorByIntensity(category: PunishmentCategory): Short {
        return when (category.intensity) {
            PunishmentCategoryIntensity.LIGHT -> 5
            PunishmentCategoryIntensity.MEDIUM -> 1
            PunishmentCategoryIntensity.MAX -> 14
        }
    }

    private fun getChatColorByIntensity(category: PunishmentCategory): ChatColor {
        return when (category.intensity) {
            PunishmentCategoryIntensity.LIGHT -> ChatColor.GREEN
            PunishmentCategoryIntensity.MEDIUM -> ChatColor.GOLD
            PunishmentCategoryIntensity.MAX -> ChatColor.RED
        }
    }
}
