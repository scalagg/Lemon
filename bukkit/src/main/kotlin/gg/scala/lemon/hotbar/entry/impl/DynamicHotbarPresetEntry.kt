package gg.scala.lemon.hotbar.entry.impl

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
class DynamicHotbarPresetEntry : HotbarPresetEntry
{
    companion object
    {
        @JvmStatic
        val DEFAULT_STACK = ItemStack(Material.AIR)
    }

    private val uniqueId = UUID.randomUUID()

    var onBuild: (Player) -> ItemStack = { DEFAULT_STACK }
    var onClick: (Player) -> Unit = {}

    override fun uniqueId(): UUID = uniqueId

    override fun buildItemStack(player: Player): ItemStack
    {
        val build = onBuild.invoke(player)

        return build

    }

    override fun onRightClick(player: Player) {
        onClick.invoke(player)
    }
}
