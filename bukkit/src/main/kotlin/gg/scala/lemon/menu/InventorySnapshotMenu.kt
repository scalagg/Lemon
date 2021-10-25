package gg.scala.lemon.menu

import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants.DOUBLE_ARROW_RIGHT
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.nms.MinecraftReflection.getPing
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.roundToInt

class InventorySnapshotMenu(player: Player) : Menu()
{
    private val boundPlayer: UUID
    private val originalInventory: Array<ItemStack?>
    private val originalArmor: Array<ItemStack?>
    private val snapshotId = UUID.randomUUID()
    private val health: Double
    private val food: Double
    private val longestCombo: Int
    private val totalHits: Int
    private val target: Player
    private val buttonMap: MutableMap<Int, Button> = HashMap()
    private var potCount = 0
    private var soupCount = 0
    private var potionMatch: Boolean
    private var soupMatch: Boolean

    override fun getTitle(player: Player): String
    {
        return "Inventory " + DOUBLE_ARROW_RIGHT + " " + target.displayName
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return buttonMap
    }

    override fun size(buttons: Map<Int, Button>): Int
    {
        return 54
    }

    companion object
    {
        private val NO_EFFECT_LIST = listOf(
            CC.RED + "No potion effects found!"
        )
        private val AIR = ItemStack(Material.AIR)
    }

    init
    {
        val contents = player.inventory.contents
        val armor = player.inventory.armorContents

        boundPlayer = player.uniqueId
        originalInventory = contents
        originalArmor = armor
        target = player

        health = player.health
        food = player.foodLevel.toDouble()

        val potionEffectStrings = ArrayList<String>()

        for (potionEffect in player.activePotionEffects)
        {
            val romanNumeral: String = MathUtil.convertToRomanNumeral(potionEffect.amplifier + 1)
            val effectName: String = StringUtil.toNiceString(potionEffect.type.name.toLowerCase())
            val duration: String = MathUtil.convertTicksToMinutes(potionEffect.duration)

            potionEffectStrings.add(CC.GRAY + " - " + CC.WHITE + effectName + " " + romanNumeral + CC.GRAY + " (" + duration + ")")
        }

        for (i in 0..8)
        {
            buttonMap[i + 27] =
                ItemBuilder((if (contents[i] == null) AIR else contents[i])!!).toButton()
            buttonMap[i + 18] =
                ItemBuilder((if (contents[i + 27] == null) AIR else contents[i + 27])!!).toButton()
            buttonMap[i + 9] =
                ItemBuilder((if (contents[i + 18] == null) AIR else contents[i + 18])!!).toButton()
            buttonMap[i] =
                ItemBuilder((if (contents[i + 9] == null) AIR else contents[i + 9])!!).toButton()
        }

        val roundedHealth = (health / 2.0 * 2.0).roundToInt() / 2.0

        buttonMap[45] = ItemBuilder(Material.SPECKLED_MELON)
            .name(CC.GRAY + "Health: " + CC.GOLD + roundedHealth)
            .amount(roundedHealth.toInt())
            .toButton()

        val roundedFood = (health / 2.0 * 2.0).roundToInt() / 2.0

        buttonMap[46] = ItemBuilder(Material.COOKED_BEEF)
            .name(CC.GRAY + "Hunger: " + CC.GOLD + roundedFood)
            .amount(roundedFood.toInt())
            .toButton()
        buttonMap[47] = ItemBuilder(Material.POTION)
            .name(CC.GOLD + "Potion Effects")
            .amount(potionEffectStrings.size)
            .setLore(
                if (potionEffectStrings.isEmpty()) NO_EFFECT_LIST else potionEffectStrings
            )
            .toButton()

        for (i in 36..39)
        {
            buttonMap[i] = ItemBuilder((if (armor[39 - i] == null) AIR else armor[39 - i])!!)
                .toButton()
        }
    }
}
