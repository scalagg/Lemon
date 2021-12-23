package gg.scala.lemon.hotbar

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import gg.scala.lemon.hotbar.entry.impl.defaults.NoHotbarPresetEntry
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
class HotbarPreset
{
    companion object
    {
        @JvmStatic
        val HOTBAR_RANGE = 0 until 9
    }

    val entries = mutableMapOf<Int, HotbarPresetEntry>()

    init
    {
        for (slot in HOTBAR_RANGE)
        {
            entries[slot] = NoHotbarPresetEntry
        }
    }

    fun addSlot(int: Int, entry: HotbarPresetEntry)
    {
        if (!HOTBAR_RANGE.contains(int))
            throw IndexOutOfBoundsException("Slot out of hotbar range")

        entries.replace(int, entry)
    }

    fun applyToPlayer(player: Player)
    {
        for (entry in entries)
        {
            player.inventory.setItem(
                entry.key, entry.value.finalizedItemStack(player)
            )
        }

        player.updateInventory()
    }

    @JvmSynthetic
    inline fun <reified T : HotbarPresetEntry> mutateSlot(
        int: Int, lambda: (T) -> Unit
    )
    {
        if (!HOTBAR_RANGE.contains(int))
            throw IndexOutOfBoundsException("Slot out of hotbar range")

        val slot = getSlotAs<T>(int)
        slot.apply(lambda)
    }

    @JvmSynthetic
    inline fun <reified T : HotbarPresetEntry> getSlotAs(int: Int): T = entries[int] as T
}
