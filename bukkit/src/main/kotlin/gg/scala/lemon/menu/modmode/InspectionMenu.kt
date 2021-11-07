package gg.scala.lemon.menu.modmode

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.buttons.StaticItemStackButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @author puugz
 * @since 07/11/2021 18:15
 */
class InspectionMenu(val target: Player) : Menu() {

    override fun getTitle(player: Player): String {
        return "${target.name}'s Inventory"
    }

    override fun getButtons(player: Player): Map<Int, Button> {
        val buttons = hashMapOf<Int, Button>()

        target.inventory.contents.forEachIndexed { index, item ->
            if (item != null) {
                buttons[index] = StaticItemStackButton(item)
            }
        }

        for (i in 36..44) {
            buttons[i] = GlassButton(15)
        }

        target.inventory.armorContents.forEachIndexed { index, item ->
            if (item != null) {
                buttons[45 + index] = StaticItemStackButton(item)
            }
        }

        buttons[49] = GlassButton(15)

        buttons[51] = ExperienceButton(target.exp, target.totalExperience, target.level)
        buttons[52] = HealthButton(target.health, target.maxHealth)
        buttons[53] = LocationButton(target.location)

        return buttons
    }

    inner class ExperienceButton(private val exp: Float, private val totalExp: Int, private val level: Int) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(Material.EXP_BOTTLE)
                .name("${CC.PRI}Experience")
                .addToLore(
                    "${CC.SEC}Level: ${CC.GRAY + level}",
                    "${CC.SEC}XP Percent: ${CC.GRAY}${(exp / totalExp) * 100}%",
                    "${CC.SEC}XP: ${CC.GRAY + exp}/${totalExp}",
                ).build()
        }
    }

    inner class HealthButton(private val health: Double, private val maxHealth: Double) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(Material.INK_SACK)
                .data(1)
                .name("${CC.PRI}Health")
                .addToLore(
                    "${CC.SEC}Health: ${CC.GRAY + String.format("%.2f", health)}/${String.format("%.2f", maxHealth)}",
                ).build()
        }
    }

    inner class LocationButton(private val location: Location) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(Material.PAPER)
                .name("${CC.PRI}Location")
                .addToLore(
                    "${CC.SEC}World: ${CC.GRAY + location.world}",
                    "${CC.SEC}X: ${CC.GRAY + String.format("%.3f", location.x)}",
                    "${CC.SEC}Y: ${CC.GRAY + String.format("%.3f", location.y)}",
                    "${CC.SEC}Z: ${CC.GRAY + String.format("%.3f", location.z)}",
                ).build()
        }
    }
}