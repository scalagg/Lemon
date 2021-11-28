package gg.scala.lemon.hotbar

import gg.scala.lemon.hotbar.entry.impl.StaticHotbarPresetEntry
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
object HotbarPresetTest
{
    fun test(player: Player)
    {
        val preset = HotbarPreset()
        preset.addSlot(
            0, StaticHotbarPresetEntry(
                ItemBuilder(Material.HOPPER)
            )
        )

        preset.mutateSlot<StaticHotbarPresetEntry>(0) {
            it.onClick = { player ->
                player.sendMessage("kid")
            }
        }

        player apply preset
    }
}

