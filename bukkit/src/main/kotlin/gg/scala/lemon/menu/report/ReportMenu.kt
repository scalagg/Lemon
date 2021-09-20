package gg.scala.lemon.menu.report

import gg.scala.lemon.Lemon
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import gg.scala.lemon.player.enums.ReportType
import gg.scala.lemon.util.other.Cooldown
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
        return hashMapOf<Int, Button>().also { buttons ->
            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)
            var index = 0

            ReportType.VALUES.forEach {
                val finalDescription = mutableListOf<String>()

                it.examples.forEach { example ->
                    finalDescription.add("${CC.GRAY} ${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}$example")
                }

                buttons[index++] = ItemBuilder(it.material)
                    .name("${CC.PRI}${it.fancyName}")
                    .setLore(finalDescription)
                    .toButton { _, _ ->
                        sendStaffMessage(
                            player,
                            "${CC.YELLOW}${coloredName(player)} ${CC.RED}reported ${CC.YELLOW}${coloredName(target)}${CC.RED} for ${CC.WHITE}${it.fancyName}${CC.RED}.",
                            true,
                            QuickAccess.MessageType.NOTIFICATION
                        ).whenComplete { _, throwable ->
                            if (throwable != null) {
                                player.sendMessage("${CC.RED}Something went wrong while submitting your report, try again later.")
                            } else {
                                lemonPlayer.cooldowns["report"] = Cooldown(60000L)

                                player.closeInventory()
                                player.sendMessage("${CC.GREEN}Your report has been submitted.")
                            }
                        }
                    }
            }
        }
    }
}
