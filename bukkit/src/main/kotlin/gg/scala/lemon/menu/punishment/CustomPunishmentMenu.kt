package gg.scala.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.OffenseTracker
import gg.scala.lemon.player.punishment.template.PunishmentSeverity
import gg.scala.lemon.util.DateUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.CompletableFuture

class CustomPunishmentMenu(
    private val targetUuid: UUID,
    private val targetName: String,
    private val category: PunishmentCategory
) : Menu() {

    init {
        placeholder = true
    }

    override fun getTitle(player: Player): String {
        return "${CC.B_GREEN}Custom ${category.fancyVersion} - $targetName"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also { buttons ->

            buttons[10] = createDurationButton(player, 1, TimeUnit.HOURS)
            buttons[11] = createDurationButton(player, 4, TimeUnit.HOURS)
            buttons[12] = createDurationButton(player, 8, TimeUnit.HOURS)
            buttons[13] = createDurationButton(player, 1, TimeUnit.DAYS)
            buttons[14] = createDurationButton(player, 3, TimeUnit.DAYS)
            buttons[15] = createDurationButton(player, 7, TimeUnit.DAYS)
            buttons[16] = createDurationButton(player, 30, TimeUnit.DAYS)

            buttons[21] = ItemBuilder(XMaterial.CLOCK)
                .name("${CC.GREEN}${CC.BOLD}Custom Duration")
                .addToLore(
                    "${CC.WHITE}Enter a custom duration",
                    "${CC.WHITE}in hours or days.",
                    "",
                    "${CC.YELLOW}Click to enter custom duration!"
                ).toButton { _, _ ->
                    promptCustomDuration(player)
                }

            buttons[23] = ItemBuilder(XMaterial.BARRIER)
                .name("${CC.RED}${CC.BOLD}Permanent ${category.fancyVersion}")
                .addToLore(
                    "${CC.WHITE}Issue a permanent ${category.fancyVersion.lowercase()}",
                    "${CC.WHITE}to $targetName.",
                    "",
                    "${CC.RED}This cannot be undone!",
                    "",
                    "${CC.YELLOW}Click to issue permanent ${category.fancyVersion.lowercase()}!"
                ).toButton { _, _ ->
                    promptReason(player, -1L)
                }

            buttons[40] = ItemBuilder(XMaterial.ARROW)
                .name("${CC.RED}${CC.BOLD}Go Back")
                .addToLore(
                    "${CC.WHITE}Return to the previous menu.",
                    "",
                    "${CC.YELLOW}Click to go back!"
                ).toButton { _, _ ->
                    PunishmentMenu(targetUuid, targetName, "Custom ${category.fancyVersion}", category).openMenu(player)
                }
        }
    }

    private fun createDurationButton(player: Player, amount: Long, unit: TimeUnit): Button {
        val duration = when (unit) {
            TimeUnit.HOURS -> amount * 60 * 60 * 1000 
            TimeUnit.DAYS -> amount * 24 * 60 * 60 * 1000 
            else -> amount
        }

        val displayText = when (unit) {
            TimeUnit.HOURS -> "$amount hour${if (amount > 1) "s" else ""}"
            TimeUnit.DAYS -> "$amount day${if (amount > 1) "s" else ""}"
            else -> amount.toString()
        }

        return ItemBuilder(
            when (unit) {
                TimeUnit.HOURS -> XMaterial.CLOCK
                TimeUnit.DAYS -> XMaterial.PAPER 
                else -> XMaterial.PAPER
            }
        ).name("${CC.GREEN}${CC.BOLD}$displayText")
            .addToLore(
                "${CC.WHITE}Issue a $displayText ${category.fancyVersion.lowercase()}",
                "${CC.WHITE}to $targetName.",
                "",
                "${CC.YELLOW}Click to select duration!"
            ).toButton { _, _ ->
                promptReason(player, duration)
            }
    }

    private fun promptCustomDuration(player: Player) {
        player.closeInventory()

        InputPrompt()
            .withText("${CC.SEC}Enter duration (e.g. '2h' for 2 hours or '3d' for 3 days)")
            .acceptInput { context, input ->
                val duration = try {
                    when {
                        input.endsWith("h", ignoreCase = true) -> {
                            input.dropLast(1).toLong() * 60 * 60 * 1000
                        }
                        input.endsWith("d", ignoreCase = true) -> {
                            input.dropLast(1).toLong() * 24 * 60 * 60 * 1000
                        }
                        else -> {
                            context.sendMessage("${CC.RED}Invalid duration format! Use 'h' for hours or 'd' for days.")
                            return@acceptInput
                        }
                    }
                } catch (e: NumberFormatException) {
                    context.sendMessage("${CC.RED}Invalid number format!")
                    return@acceptInput
                }

                promptReason(player, duration)
            }.start(player)
    }

    private fun promptReason(player: Player, duration: Long) {
        InputPrompt()
            .withText("${CC.SEC}Enter reason for the ${category.fancyVersion.lowercase()}")
            .acceptInput { context, input ->
                if (input.equals("cancel", ignoreCase = true)) {
                    context.sendMessage("${CC.RED}Cancelled punishment.")
                    return@acceptInput
                }

                Tasks.async {
                    try {
                        handlePunishmentForTargetPlayerGlobally(
                            issuer = player,
                            uuid = targetUuid,
                            category = category,
                            duration = duration,
                            reason = input,
                            silent = true
                        )
                        OffenseTracker.incrementOffense(
                            targetUuid, 
                            category, 
                            if (duration == -1L) PunishmentSeverity.EXTREME else PunishmentSeverity.LOW
                        )
                        context.sendMessage("${CC.GREEN}Successfully ${category.fancyVersion.lowercase()}d $targetName for $input!")
                        player.closeInventory()
                    } catch (e: Exception) {
                        context.sendMessage("${CC.RED}Failed to ${category.fancyVersion.lowercase()} $targetName: ${e.message ?: "Unknown error"}")
                    }
                }
            }.start(player)
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 45 
    }
} 