package gg.scala.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.Lemon
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.util.CubedCacheUtil
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

    init {
        placeholder = true
    }

    override fun getTitle(player: Player): String {
        val name = CubedCacheUtil.fetchName(uuid)!!
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} ${coloredName(name)}"

        return when (viewType) {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            var index = 10

            PunishmentCategory.values().forEach {
                val totalAmount = punishments.filter { punishment ->
                    punishment.category == it
                }.size

                buttons[index] = ItemBuilder(XMaterial.WHITE_WOOL)
                    .name("${getChatColorByIntensity(it)}${it.fancyVersion + "s"}")
                    .data(14)
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

                index += 2
            }
        }
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

    private fun getChatColorByIntensity(category: PunishmentCategory): ChatColor {
        return when (category.intensity) {
            PunishmentCategoryIntensity.LIGHT -> ChatColor.GOLD
            PunishmentCategoryIntensity.MEDIUM -> ChatColor.RED
        }
    }
}
