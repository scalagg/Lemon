package gg.scala.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.player.punishment.OffenseTracker
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.template.PunishmentSeverity
import gg.scala.lemon.player.punishment.template.PunishmentTemplate
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import gg.scala.lemon.util.DateUtil
import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.handler.PunishmentHandler.handleUnPunishmentForTargetPlayerGlobally
import gg.scala.lemon.util.QuickAccess.parseReason
import java.util.concurrent.CompletableFuture

/**
 * Menu for issuing punishments with offense tracking and severity levels
 * @author Pawful
 */
class PunishmentMenu(
    private val targetUuid: UUID,
    private val targetName: String,
    private val reason: String,
    private val category: PunishmentCategory? = null
) : Menu() {

    init {
        placeholder = true
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String {
        return "${CC.B_GREEN}Punish $targetName"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()
        // Glass pane border
        val borderSlots = listOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 17,
            18, 26,
            27, 35,
            36, 37, 38, 39, 40, 41, 42, 43, 44
        )
        borderSlots.forEach { slot ->
            buttons[slot] = ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .toButton { _, _ -> }
        }
        // Player head and reason at top center
        buttons[4] = ItemBuilder(XMaterial.PLAYER_HEAD)
            .name("${CC.GREEN}${CC.BOLD}$targetName")
            .addToLore("${CC.YELLOW}Reason: ${CC.WHITE}$reason")
            .toButton { _, _ -> }
        // Centered category buttons
        if (category == null) {
            // Chat Offense (center left)
            buttons[20] = ItemBuilder(XMaterial.WRITABLE_BOOK)
                .name("${CC.AQUA}${CC.BOLD}Chat Offense")
                .addToLore(
                    "${CC.WHITE}Verbal Abuse, Spam, Harassment, Trolling, etc",
                    "",
                    "${CC.YELLOW}Click to view chat offense punishments!"
                ).toButton { _, _ ->
                    PunishmentMenu(targetUuid = targetUuid, targetName = targetName, reason = reason, category = PunishmentCategory.MUTE).openMenu(player)
                }
            // General Offense (center)
            buttons[22] = ItemBuilder(XMaterial.HOPPER)
                .name("${CC.GOLD}${CC.BOLD}General Offense")
                .addToLore(
                    "${CC.WHITE}Command/Map/Class/Skill exploits, etc",
                    "",
                    "${CC.YELLOW}Click to view general offense punishments!"
                ).toButton { _, _ ->
                    PunishmentMenu(targetUuid = targetUuid, targetName = targetName, reason = reason, category = PunishmentCategory.BAN).openMenu(player)
                }
            // Client Mod (center right)
            buttons[24] = ItemBuilder(XMaterial.IRON_SWORD)
                .name("${CC.LIGHT_PURPLE}${CC.BOLD}Client Mod")
                .addToLore(
                    "${CC.WHITE}X-ray, Forcefield, Speed, Fly, Inventory Hacks, etc",
                    "",
                    "${CC.YELLOW}Click to view client modification punishments!"
                ).toButton { _, _ ->
                    PunishmentMenu(targetUuid = targetUuid, targetName = targetName, reason = reason, category = PunishmentCategory.BAN).openMenu(player)
                }
            // Back (bottom left)
            buttons[36] = ItemBuilder(XMaterial.ARROW)
                .name("${CC.RED}${CC.BOLD}Back")
                .addToLore("${CC.YELLOW}Return to previous menu.")
                .toButton { _, _ -> player.closeInventory() }
            // Close (bottom right)
            buttons[44] = ItemBuilder(XMaterial.BARRIER)
                .name("${CC.RED}${CC.BOLD}Close")
                .addToLore("${CC.YELLOW}Click to close this menu.")
                .toButton { _, _ -> player.closeInventory() }
            return buttons
        }
        // Show severity sections for the selected category
        var index = 10
        PunishmentSeverity.entries.forEach { severity ->
            // Add severity header
            buttons[index] = ItemBuilder(
                when (severity) {
                    PunishmentSeverity.LOW -> XMaterial.LIME_WOOL
                    PunishmentSeverity.MEDIUM -> XMaterial.YELLOW_WOOL
                    PunishmentSeverity.HIGH -> XMaterial.RED_WOOL
                    PunishmentSeverity.EXTREME -> XMaterial.BLACK_WOOL
                }
            ).name("${Color.translate(severity.color)}${severity.displayName} Severity")
                .addToLore(
                    "${CC.WHITE}${severity.displayName} severity punishments",
                    "${CC.WHITE}for $targetName.",
                    "",
                    "${CC.GRAY}These punishments are for",
                    "${CC.GRAY}${severity.displayName.lowercase()} severity violations."
                ).toButton { _, _ -> }

            index += 9

            // Add templates for this severity and category
            PunishmentTemplate.getTemplatesForCategory(category)
                .filter { it.severity == severity }
                .forEach { template ->
                    buttons[index] = createTemplateButton(template, player)
                    index++

                    if (index % 9 == 8) index += 2
                }

            index += 9 - (index % 9)
        }

        // Custom punishment button
        buttons[40] = ItemBuilder(XMaterial.ANVIL)
            .name("${CC.GREEN}${CC.BOLD}Custom Punishment")
            .addToLore(
                "${CC.WHITE}Issue a custom punishment",
                "${CC.WHITE}with specific duration and reason.",
                "",
                "${CC.YELLOW}Click to create custom punishment!"
            ).toButton { _, _ ->
                CustomPunishmentMenu(targetUuid, targetName, category).openMenu(player)
            }

        // Show punishment history if player has mod permissions
        if (player.hasPermission("lemon.mod")) {
            displayPunishmentHistory(buttons, player)
        }
        return buttons
    }

    private fun createTemplateButton(template: PunishmentTemplate, player: Player): Button {
        val durationText: String = if (template.duration == -1L) "Permanent" else { 
            val hours = template.duration / (1000 * 60 * 60)
            val days = hours / 24
            if (days > 0) "$days day${if (days > 1) "s" else ""}" else "$hours hour${if (hours > 1) "s" else ""}"
        }
        val offenseCount: Int = OffenseTracker.getOffenseCount(targetUuid, template.category, template.severity)

        return ItemBuilder(
            when (template.category) {
                PunishmentCategory.BAN -> XMaterial.BARRIER
                PunishmentCategory.MUTE -> XMaterial.PAPER
                PunishmentCategory.KICK -> XMaterial.DIAMOND_BOOTS
                else -> XMaterial.PAPER
            }
        ).name("${Color.translate(template.severity.color)}${template.reason}")
            .addToLore(
                "${CC.WHITE}Category: ${CC.YELLOW}${template.category.fancyVersion}",
                "${CC.WHITE}Duration: ${CC.YELLOW}$durationText",
                "${CC.WHITE}Severity: ${Color.translate(template.severity.color)}${template.severity.displayName}",
                "${CC.WHITE}Past Offenses: ${CC.YELLOW}$offenseCount",
                if (template.requiresAppeal) "${CC.RED}Requires Appeal" else "",
                "",
                "${CC.YELLOW}Click to issue this punishment!"
            ).toButton { _, _ ->
                if (!player.hasPermission("lemon.punishment.${template.category.name.lowercase()}")) {
                    player.sendMessage("${CC.RED}You don't have permission to ${template.category.fancyVersion.lowercase()} players!")
                    return@toButton
                }

                Tasks.async {
                    try {
                        handlePunishmentForTargetPlayerGlobally(
                            issuer = player,
                            uuid = targetUuid,
                            category = template.category,
                            duration = template.duration,
                            reason = template.reason,
                            silent = true
                        )
                        OffenseTracker.incrementOffense(targetUuid, template.category, template.severity)
                        player.sendMessage("${CC.GREEN}Successfully ${template.category.fancyVersion.lowercase()}d $targetName for ${template.reason}!")
                        if (template.requiresAppeal) {
                            player.sendMessage("${CC.YELLOW}This punishment requires an appeal.")
                        }
                        player.closeInventory()
                    } catch (e: Exception) {
                        player.sendMessage("${CC.RED}Failed to ${template.category.fancyVersion.lowercase()} $targetName: ${e.message ?: "Unknown error"}")
                    }
                }
            }
    }

    private fun displayPunishmentHistory(buttons: MutableMap<Int, Button>, player: Player) {
        var historySlot = 45
        val history = PunishmentHandler.fetchAllPunishmentsForTarget(targetUuid).get()
        
        history.forEach { punishment ->
            if (historySlot >= 54) return@forEach
            
            buttons[historySlot] = ItemBuilder(
                when (punishment.category) {
                    PunishmentCategory.BAN -> XMaterial.BARRIER
                    PunishmentCategory.MUTE -> XMaterial.PAPER
                    PunishmentCategory.KICK -> XMaterial.DIAMOND_BOOTS
                    else -> XMaterial.PAPER
                }
            ).name("${CC.GREEN}${CC.BOLD}${punishment.category.fancyVersion}")
                .addToLore(
                    "${CC.WHITE}Reason: ${CC.YELLOW}${punishment.addedReason}",
                    "${CC.WHITE}Staff: ${CC.YELLOW}${punishment.addedBy}",
                    "${CC.WHITE}Date: ${CC.YELLOW}${punishment.addedOn}",
                    if (punishment.removedBy != null) "${CC.WHITE}Removed by: ${CC.YELLOW}${punishment.removedBy}" else "",
                    if (punishment.removedOn != null) "${CC.WHITE}Removed on: ${CC.YELLOW}${punishment.removedOn!!}" else ""
                ).toButton { _, _ -> }
            
            historySlot++
        }
    }

    override fun size(buttons: Map<Int, Button>): Int {
        return 54 // 6 rows
    }
} 