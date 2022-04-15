package gg.scala.lemon.menu.grant.context

import com.cryptomorin.xseries.XMaterial
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
    private val rank: Rank
) : PaginatedMenu() {

    companion object {
        @JvmStatic
        val durations = listOf(
            Duration.parse("5m"), Duration.parse("1d"),
            Duration.parse("7d"), Duration.parse("1mo"), Duration.parse("3mo"),
            Duration.parse("6mo"), Duration.parse("1y"), Duration.parse("3y"),
        )
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Grant ${Constants.DOUBLE_ARROW_RIGHT} $name ${Constants.DOUBLE_ARROW_RIGHT} Time"
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            it[3] = ItemBuilder(XMaterial.MAP)
                .name("${CC.B_GREEN}Custom Duration")
                .addToLore(
                    "${CC.GRAY}Input a custom duration",
                    "${CC.GRAY}to use on this grant.",
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

                                GrantDurationContextMenu(uuid, name, rank).openMenu(player)
                            } else {
                                GrantReasonContextMenu(uuid, name, rank, duration).openMenu(player)

                                context.sendMessage("${CC.SEC}You've set the ${CC.PRI}Duration${CC.SEC} to ${CC.WHITE}$input${CC.SEC}.")
                            }
                        }.start(player)
                }

            it[5] = ItemBuilder(XMaterial.COMPASS)
                .name("${CC.B_RED}Permanent")
                .addToLore(
                    "${CC.GRAY}Apply this grant permanently.",
                    "",
                    "${CC.YELLOW}Click to continue."
                )
                .toButton { _, _ ->
                    GrantReasonContextMenu(uuid, name, rank, Duration.parse("perm")).openMenu(player)

                    player.sendMessage("${CC.SEC}You've set the ${CC.PRI}Duration${CC.SEC} to ${CC.WHITE}Permanent${CC.SEC}.")
                }
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            durations.forEach { duration ->
                it[it.size] = DurationButton(duration)
            }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Schedulers.sync().runLater({
                GrantRankContextMenu(uuid, name).openMenu(player)
            }, 1L)
        }
    }

    private inner class DurationButton(
        private val duration: Duration
    ) : Button() {

        private val formatted = DurationFormatUtils.formatDurationWords(duration.get(), true, true)

        override fun getName(player: Player): String {
            return "${CC.PRI}$formatted"
        }

        override fun getMaterial(player: Player): XMaterial {
            return XMaterial.OAK_SIGN
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            GrantReasonContextMenu(uuid, name, rank, duration).openMenu(player)

            player.sendMessage("${CC.SEC}You've set the ${CC.PRI}Duration${CC.SEC} to ${CC.WHITE}$formatted${CC.SEC}.")
        }
    }
}
