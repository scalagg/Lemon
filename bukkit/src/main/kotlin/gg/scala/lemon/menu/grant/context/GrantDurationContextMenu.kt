package gg.scala.lemon.menu.grant.context

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.menu.grant.context.scope.ScopeSelectionMenu
import gg.scala.lemon.player.rank.Rank
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.time.Duration
import org.apache.commons.lang.time.DurationFormatUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import java.util.*

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
class GrantDurationContextMenu(
    private val uuid: UUID,
    private val name: String,
    private val rank: Rank,
    private val colored: String,
    private val scopes: List<String> = listOf()
) : PaginatedMenu() {

    companion object {
        @JvmStatic
        val durations = mutableMapOf(
            Duration.parse("5m") to "5 minutes",
            Duration.parse("1d") to "1 day",
            Duration.parse("7d") to "1 week",
            Duration.parse("1mo") to "1 month",
            Duration.parse("3mo") to "3 months",
            Duration.parse("6mo") to "6 months",
            Duration.parse("1y") to "1 year",
            Duration.parse("3y") to "3 years"
        )
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Duration for $colored${CC.D_GRAY}"
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            it[3] = ItemBuilder(XMaterial.MAP)
                .name("${CC.B_GREEN}Custom Duration")
                .addToLore(
                    "${CC.WHITE}Input a custom duration",
                    "${CC.WHITE}to use on this grant.",
                    "",
                    "${CC.YELLOW}Click to set duration."
                )
                .toButton { _, _ ->
                    player.closeInventory()

                    InputPrompt()
                        .withText("${CC.SEC}Please enter the ${CC.PRI}Duration${CC.SEC}.")
                        .acceptInput { context, input ->
                            val duration = try {
                                Duration.parse(input)
                            } catch (ignored: Exception) {
                                null
                            }

                            if (duration == null) {
                                context.sendMessage("Invalid duration parsed. Returning to menu. (Example: 1h30m)")

                                GrantDurationContextMenu(uuid, name, rank, colored, scopes).openMenu(player)
                            } else {
                                GrantReasonContextMenu(uuid, name, rank, duration, colored, scopes).openMenu(player)

                                context.sendMessage("${CC.SEC}You've set the ${CC.PRI}Duration${CC.SEC} to ${CC.WHITE}$input${CC.SEC}.")
                            }
                        }.start(player)
                }

            it[5] = ItemBuilder(XMaterial.COMPASS)
                .name("${CC.B_RED}Permanent")
                .addToLore(
                    "${CC.WHITE}Apply this grant permanently.",
                    "",
                    "${CC.YELLOW}Click to continue."
                )
                .toButton { _, _ ->
                    GrantReasonContextMenu(uuid, name, rank, Duration.parse("perm"), colored, scopes).openMenu(player)

                    player.sendMessage("${CC.SEC}You've set the ${CC.PRI}Duration${CC.SEC} to ${CC.WHITE}Permanent${CC.SEC}.")
                }
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            durations.forEach { duration ->
                it[it.size] = DurationButton(duration.key, duration.value)
            }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Schedulers.sync().runLater({
                if (scopes.isNotEmpty())
                {
                    ScopeSelectionMenu(uuid, name, colored, rank, scopes.toMutableList()).openMenu(player)
                    return@runLater
                }

                GrantRankContextMenu(uuid, name, colored).openMenu(player)
            }, 1L)
        }
    }

    private inner class DurationButton(
        private val duration: Duration,
        private val identifier: String
    ) : Button() {

        override fun getName(player: Player): String {
            return "${CC.WHITE}$identifier"
        }

        override fun getMaterial(player: Player): XMaterial {
            return XMaterial.PAPER
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            GrantReasonContextMenu(uuid, name, rank, duration, colored, scopes).openMenu(player)

            player.sendMessage("${CC.SEC}You've set the ${CC.PRI}Duration${CC.SEC} to ${CC.WHITE}$identifier${CC.SEC}.")
        }
    }
}
