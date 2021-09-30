package gg.scala.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess.coloredName
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

    private val name = CubedCacheUtil.fetchName(uuid)

    override fun getTitle(player: Player): String {
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} ${coloredName(name)}"

        return when (viewType) {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            var index = 10

            PunishmentCategory.VALUES_2.forEach {
                val totalAmount = punishments.filter { punishment ->
                    punishment.category == it
                }.size
                val active = punishments.filter { punishment ->
                    punishment.category == it && punishment.isActive
                }.size

                buttons[index] = ItemBuilder(XMaterial.WHITE_WOOL)
                    .name("${CC.RED}${it.fancyVersion + "s"}")
                    .data(if (active >= 1) 5 else 14)
                    .amount(totalAmount)
                    .addToLore(
                        "${CC.GRAY}Viewing statistics for the",
                        "${CC.GRAY}${it.fancyVersion} category:",
                        "",
                        " ${CC.GRAY}Total: ${CC.WHITE}${totalAmount}",
                        " ${CC.GRAY}Active: ${CC.GREEN}${active}",
                        " ${CC.GRAY}Inactive: ${CC.RED}${
                            punishments.filter { punishment ->
                                punishment.category == it && punishment.isRemoved
                            }.size
                        }",
                        "",
                        "${CC.YELLOW}Click to view more info."
                    ).toButton { _, _ ->
                        fetchPunishments(it).whenComplete { list, _ ->
                            if (list.isEmpty()) {
                                player.sendMessage("${CC.YELLOW}$name${CC.RED} has no recorded ${CC.YELLOW}${it.fancyVersion.toLowerCase()}s${CC.RED}.")
                                return@whenComplete
                            }

                            PunishmentDetailedViewMenu(
                                uuid, it, viewType, list
                            ).openMenu(player)
                        }
                    }

                index += 2
            }
        }
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 27
    }

    private fun fetchPunishments(category: PunishmentCategory): CompletableFuture<List<Punishment>> {
        return when (viewType) {
            HistoryViewType.TARGET_HIST -> {
                PunishmentHandler.fetchPunishmentsForTargetOfCategory(uuid, category)
            }
            HistoryViewType.STAFF_HIST -> {
                PunishmentHandler.fetchPunishmentsByExecutorOfCategory(uuid, category)
            }
        }
    }
}
