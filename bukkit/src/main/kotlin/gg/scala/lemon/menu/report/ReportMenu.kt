package gg.scala.lemon.menu.report

import gg.scala.lemon.cooldown.CooldownHandler
import gg.scala.lemon.cooldown.impl.ReportCooldown
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.enums.ReportType
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.sendStaffMessageWithFlag
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
class ReportMenu(
    private val target: Player
) : Menu() {

    override fun getTitle(player: Player): String {
        return "Reporting ${Constants.DOUBLE_ARROW_RIGHT} ${target.name}"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->
            ReportType.VALUES.forEach {
                val finalDescription = mutableListOf<String>()

                it.examples.forEach { example ->
                    finalDescription.add("${CC.GRAY} ${Constants.DOUBLE_ARROW_RIGHT} ${CC.WHITE}$example")
                }

                buttons[buttons.size] = ItemBuilder(it.material)
                    .name("${CC.PRI}${it.fancyName}")
                    .setLore(finalDescription)
                    .toButton { _, _ ->
                        sendStaffMessageWithFlag(
                            player,
                            "${CC.YELLOW}${coloredName(player)} ${CC.RED} reported ${CC.YELLOW}${coloredName(target)}${CC.RED} for ${it.fancyName}.",
                            true,
                            QuickAccess.MessageType.NOTIFICATION,
                            "reports-disabled"
                        ).whenComplete { _, throwable ->
                            if (throwable != null) {
                                player.sendMessage("${CC.RED}Something went wrong while processing your report, try again later.")
                            } else {
                                val report = CooldownHandler.find(
                                    ReportCooldown::class.java
                                )

                                report?.addOrOverride(player)

                                player.closeInventory()
                                player.sendMessage("${CC.GREEN}Your report has been submitted.")
                            }
                        }
                    }
            }
        }
    }
}
