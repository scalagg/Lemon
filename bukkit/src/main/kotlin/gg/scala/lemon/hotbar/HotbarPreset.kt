package gg.scala.lemon.hotbar

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import gg.scala.lemon.hotbar.entry.impl.defaults.NoHotbarPresetEntry
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.entity.Player
import java.util.UUID

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
        HotbarEntryStore[entry.uniqueId().toString()] = entry
    }

    fun applyToPlayer(player: Player)
    {
        for (entry in entries)
        {
            val finalized = entry.value
                .finalizedItemStack(player)
                ?: continue

            val finalizedAndTagged = ItemUtils
                .addToItemTag(
                    finalized,
                    "invokerc",
                    entry.value.uniqueId().toString()
                )

            player.inventory.setItem(
                entry.key, finalizedAndTagged
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
