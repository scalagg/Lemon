package gg.scala.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
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
    private val punishments: List<Punishment>,
    private val removed: List<Punishment>,
    private val colored: String
) : Menu()
{
    init
    {
        placeholder = true
    }

    private val name = CubedCacheUtil.fetchName(uuid)

    override fun getTitle(player: Player): String
    {
        val base = "History ${Constants.DOUBLE_ARROW_RIGHT} $colored"

        return when (viewType)
        {
            HistoryViewType.STAFF_HIST -> "Staff $base"
            HistoryViewType.TARGET_HIST -> base
        }
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return hashMapOf<Int, Button>().also { buttons ->
            var index = 10

            PunishmentCategory.VALUES_2.forEach {
                val totalAmount = punishments.filter { punishment ->
                    punishment.category == it
                }.size
                val active = punishments.filter { punishment ->
                    punishment.category == it && punishment.isActive
                }.size

                buttons[index] = ItemBuilder(
                    if (totalAmount == 0) XMaterial.BARRIER else (if (active >= 1 && viewType == HistoryViewType.TARGET_HIST)
                        XMaterial.LIME_WOOL else XMaterial.RED_WOOL)
                )
                    .name(
                        "${CC.B_GREEN}${
                            when (viewType)
                            {
                                HistoryViewType.STAFF_HIST -> "Issued "
                                HistoryViewType.TARGET_HIST -> ""
                            }
                        }${it.fancyVersion + "s"}"
                    )
                    .amount(
                        if (totalAmount == 0) 1 else (if (totalAmount >= 64) 64 else totalAmount)
                    )
                    .addToLore(
                        "${CC.WHITE}Viewing statistics for this",
                        "${CC.WHITE}category:",
                        "",
                        "${CC.WHITE}Total: ${CC.B_GREEN}$totalAmount",
                        "${CC.WHITE}Active: ${CC.B_GREEN}$active",
                        "${CC.WHITE}Inactive: ${CC.B_GREEN}${
                            punishments.filter { punishment ->
                                punishment.category == it && punishment.isRemoved
                            }.size
                        }",
                        "",
                        "${CC.YELLOW}Click to view more!"
                    ).toButton { _, _ ->
                        fetchPunishments(it).whenComplete { list, _ ->
                            if (list.isEmpty())
                            {
                                player.sendMessage(
                                    "${CC.RED}No entries were found within this punishment category."
                                )
                                return@whenComplete
                            }

                            Tasks.sync {
                                PunishmentDetailedViewMenu(
                                    uuid, it, viewType, list, colored
                                ).openMenu(player)
                            }
                        }
                    }

                index += 2
            }

            if (viewType == HistoryViewType.STAFF_HIST)
            {
                var newIndex = 20

                val persistentHasPermission = PunishmentCategory.PERSISTENT.filter {
                    player.hasPermission("lemon.punishment.remove.${it.name.lowercase()}")
                }

                for (it in persistentHasPermission)
                {
                    val totalAmount = removed.filter { punishment ->
                        punishment.category == it
                    }.size

                    buttons[newIndex] = ItemBuilder(XMaterial.PAPER)
                        .name(
                            "${CC.B_GREEN}${
                                when (viewType)
                                {
                                    HistoryViewType.STAFF_HIST -> "Removed "
                                    HistoryViewType.TARGET_HIST -> ""
                                }
                            }${it.fancyVersion + "s"}"
                        )
                        .amount(
                            if (totalAmount >= 64) 64 else totalAmount
                        )
                        .addToLore(
                            "${CC.WHITE}Viewing statistics for this",
                            "${CC.WHITE}category:",
                            "",
                            "${CC.WHITE}Total: ${CC.B_GREEN}${totalAmount}",
                            "",
                            "${CC.YELLOW}Click to view more!"
                        ).toButton { _, _ ->
                            fetchPunishmentsRemoved(it).whenComplete { list, _ ->
                                if (list.isEmpty())
                                {
                                    player.sendMessage("${CC.YELLOW}$name${CC.RED} has not removed any ${CC.YELLOW}${it.fancyVersion.lowercase()}s${CC.RED}.")
                                    return@whenComplete
                                }

                                PunishmentDetailedViewMenu(
                                    uuid, it, viewType, list, colored
                                ).openMenu(player)
                            }
                        }

                    newIndex += 2
                }
            }
        }
    }

    override fun size(
        buttons: Map<Int, Button>
    ): Int
    {
        return when (viewType)
        {
            HistoryViewType.TARGET_HIST -> 27
            HistoryViewType.STAFF_HIST -> 36
        }
    }

    private fun fetchPunishmentsRemoved(category: PunishmentCategory): CompletableFuture<List<Punishment>>
    {
        return PunishmentHandler.fetchPunishmentsRemovedByOfCategory(uuid, category)
    }

    private fun fetchPunishments(category: PunishmentCategory): CompletableFuture<List<Punishment>>
    {
        return when (viewType)
        {
            HistoryViewType.TARGET_HIST ->
            {
                PunishmentHandler.fetchPunishmentsForTargetOfCategory(uuid, category)
            }
            HistoryViewType.STAFF_HIST ->
            {
                PunishmentHandler.fetchPunishmentsByExecutorOfCategory(uuid, category)
            }
        }
    }
}
