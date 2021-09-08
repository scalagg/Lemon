package com.solexgames.lemon.menu.report

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.enums.ReportType
import com.solexgames.lemon.util.other.Cooldown
import com.solexgames.lemon.util.quickaccess.MessageType
import com.solexgames.lemon.util.quickaccess.sendStaffMessage
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
class ReportMenu(private val target: Player) : Menu() {

    override fun getTitle(player: Player): String {
        return "Reporting ${Constants.DOUBLE_ARROW_RIGHT} ${target.name}"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
        val buttons = mutableMapOf<Int, Button>(); var int = 0

        ReportType.VALUES.forEach {
            val finalDescription = mutableListOf<String>()

            it.examples.forEach { example ->
                finalDescription.add("${CC.GRAY} ${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}$example")
            }

            buttons[int++] = ItemBuilder(it.material)
                .name("${CC.PRI}${it.fancyName}")
                .setLore(finalDescription)
                .data(it.data).toButton() { _, _ ->
                    sendStaffMessage(
                        player,
                        "${CC.YELLOW}${player.name} ${CC.RED}reported ${CC.YELLOW}${target.name}${CC.RED} for ${CC.WHITE}${it.fancyName}${CC.RED}.",
                        true,
                        MessageType.NOTIFICATION
                    ).whenComplete { _, throwable ->
                        if (throwable != null) {
                            player.sendMessage("${CC.RED}Something went wrong while submitting your report, try again later.")
                        } else {
                            lemonPlayer.reportCooldown = Cooldown(60000L)

                            player.closeInventory()
                            player.sendMessage("${CC.GREEN}Your report has been submitted.")
                        }
                    }
                }
        }

        return buttons
    }
}
