package gg.scala.lemon.menu.modmode

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.buttons.GlassButton
import net.evilblock.cubed.menu.buttons.StaticItemStackButton
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect

/**
 * @author puugz
 * @since 07/11/2021 18:15
 */
class InspectionMenu(val target: Player) : Menu() {

    init {
        autoUpdate = true
    }

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

        target.inventory.armorContents.reversed().forEachIndexed { index, item ->
            if (item != null) {
                buttons[45 + index] = StaticItemStackButton(item)
            }
        }

        buttons[49] = GlassButton(15)
        buttons[50] = EffectsButton(target.activePotionEffects)
        buttons[51] = ExperienceButton(target.exp, target.totalExperience, target.level)
        buttons[52] = HealthButton(target.health, target.maxHealth)
        buttons[53] = LocationButton(target.location)

        return buttons
    }

    inner class EffectsButton(private val effects: Collection<PotionEffect>) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(XMaterial.POTION)
                .name("${CC.PRI}Potion Effects")
                .apply {
                    if (effects.isEmpty()) {
                        addToLore("${CC.RED}No active potion effects.")
                    } else {
                        effects.forEach {
                            addToLore("${QuickAccess.toNiceString(it.type.name.lowercase())} ${it.amplifier + 1} - ${TimeUtil.formatIntoMMSS(it.duration)}")
                        }
                    }
                }
                .build()
        }
    }

    inner class ExperienceButton(private val exp: Float, private val totalExp: Int, private val level: Int) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(XMaterial.EXPERIENCE_BOTTLE)
                .name("${CC.PRI}Experience")
                .addToLore(
                    "${CC.GRAY}Level: ${CC.WHITE + level}",
                    "${CC.GRAY}XP Percent: ${CC.WHITE}${(exp / totalExp) * 100}%",
                    "${CC.GRAY}XP: ${CC.WHITE + exp}/${totalExp}",
                ).build()
        }
    }

    inner class HealthButton(private val health: Double, private val maxHealth: Double) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(XMaterial.RED_DYE)
                .data(1)
                .name("${CC.PRI}Health")
                .addToLore(
                    "${CC.GRAY}Health: ${CC.WHITE + String.format("%.2f", health)}/${String.format("%.2f", maxHealth)}",
                ).build()
        }
    }

    inner class LocationButton(private val location: Location) : Button() {
        override fun getButtonItem(player: Player): ItemStack {
            return ItemBuilder(XMaterial.PAPER)
                .name("${CC.PRI}Location")
                .addToLore(
                    "${CC.GRAY}World: ${CC.WHITE + location.world.name}",
                    "${CC.GRAY}X: ${CC.WHITE + String.format("%.3f", location.x)}",
                    "${CC.GRAY}Y: ${CC.WHITE + String.format("%.3f", location.y)}",
                    "${CC.GRAY}Z: ${CC.WHITE + String.format("%.3f", location.z)}",
                ).build()
        }
    }
}
