package gg.scala.lemon.hotbar

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import gg.scala.lemon.hotbar.entry.impl.defaults.NoHotbarPresetEntry
import org.bukkit.entity.Player
import kotlin.reflect.KClass

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
class HotbarPreset
{
    companion object
    {
        @JvmStatic
        val HOTBAR_RANGE = 0 until 8
    }

    val entries = mutableMapOf<Int, HotbarPresetEntry>()

    init
    {
        for (slot in 0 until 8)
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
    }

    inline fun <reified T : Any> mutateSlot(
        int: Int, lambda: (T) -> Unit
    )
    {
        if (!HOTBAR_RANGE.contains(int))
            throw IndexOutOfBoundsException("Slot out of hotbar range")

        val slot = getSlotAs<T>(int)
        slot.apply(lambda)
    }

    inline fun <reified T : Any> getSlotAs(int: Int): T = entries[int] as T
}