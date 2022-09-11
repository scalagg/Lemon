package gg.scala.lemon.menu.grant.context

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.rank.Rank
import me.lucko.helper.Schedulers
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.ConfirmMenu
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.time.Duration
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
class GrantReasonContextMenu(
    private val uuid: UUID,
    private val name: String,
    private val rank: Rank,
    private val duration: Duration,
    private val colored: String,
    private val scopes: List<String> = listOf()
) : PaginatedMenu() {

    companion object {
        @JvmStatic
        val reasons = listOf(
            "Rank Migration", "Buycraft Issues", "Promotion",
            "Demotion", "Giveaway Winner", "Event Winner"
        )
    }

    override fun getPrePaginatedTitle(player: Player): String {
        return "Grant ${Constants.DOUBLE_ARROW_RIGHT} $colored${CC.D_GRAY} ${Constants.DOUBLE_ARROW_RIGHT} Reason"
    }

    override fun getGlobalButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            it[3] = ItemBuilder(XMaterial.MAP)
                .name("${CC.B_GREEN}Custom Reason")
                .addToLore(
                    "${CC.WHITE}Input a custom reason",
                    "${CC.WHITE}to use on this grant.",
                    "",
                    "${CC.YELLOW}Click to set reason."
                )
                .toButton { _, _ ->
                    player.closeInventory()

                    InputPrompt()
                        .withText("${CC.SEC}Please enter the ${CC.PRI}Reason${CC.SEC}.")
                        .acceptInput { context, input ->
                            finalizeGrant(context, input)

                            context.sendMessage("${CC.SEC}You've set the ${CC.PRI}Reason${CC.SEC} to ${CC.WHITE}$input${CC.SEC}.")
                        }.start(player)
                }

            it[5] = ItemBuilder(XMaterial.COMPASS)
                .name("${CC.B_RED}Unspecified")
                .addToLore(
                    "${CC.WHITE}Apply this grant unspecified.",
                    "",
                    "${CC.YELLOW}Click to continue."
                )
                .toButton { _, _ ->
                    finalizeGrant(player, "Unspecified")

                    player.sendMessage("${CC.SEC}You've set the ${CC.PRI}Reason${CC.SEC} to ${CC.WHITE}Unspecified${CC.SEC}.")
                }
        }
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            reasons.forEach { reason ->
                it[it.size] = ItemBuilder(XMaterial.PAPER)
                    .name(reason)
                    .toButton { _, _ ->
                        finalizeGrant(player, reason)

                        player.sendMessage("${CC.SEC}You've set the ${CC.PRI}Reason${CC.SEC} to ${CC.WHITE}$reason${CC.SEC}.")
                    }
            }
        }
    }

    private fun finalizeGrant(player: Player, reason: String) {
        ConfirmMenu(
            "Grant ${Constants.DOUBLE_ARROW_RIGHT} $name ${Constants.DOUBLE_ARROW_RIGHT} Confirm",
            listOf(
                "${CC.WHITE}Set $colored's${CC.WHITE} rank to:",
                "${CC.WHITE}${rank.getColoredName()}"
            ),
            true
        ) {
            player.closeInventory()

            if (it) {
                val grant = Grant(
                    UUID.randomUUID(),
                    uuid,
                    rank.uuid,
                    player.uniqueId,
                    System.currentTimeMillis(),
                    Lemon.instance.settings.id,
                    reason, duration.get()
                )

                grant.scopes.clear()
                grant.scopes += this.scopes
                    .ifEmpty {
                        listOf("global")
                    }

                GrantHandler.handleGrant(player, grant)
            } else {
                player.sendMessage("${CC.RED}You've cancelled the grant operation.")
            }
        }.openMenu(player)
    }

    override fun onClose(player: Player, manualClose: Boolean) {
        if (manualClose) {
            Schedulers.sync().runLater({
                GrantDurationContextMenu(uuid, name, rank, colored).openMenu(player)
            }, 1L)
        }
    }
}
