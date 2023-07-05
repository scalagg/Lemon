package gg.scala.lemon.hotbar.entry.impl

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
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

    override fun buildItemStack(player: Player) = onBuild.invoke(player)

    override fun onRightClick(player: Player) {
        onClick.invoke(player)
    }
}
