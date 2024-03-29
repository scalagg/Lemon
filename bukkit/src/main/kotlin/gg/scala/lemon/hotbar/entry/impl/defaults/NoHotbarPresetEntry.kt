package gg.scala.lemon.hotbar.entry.impl.defaults

import gg.scala.lemon.hotbar.HotbarEntryStore
import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
object NoHotbarPresetEntry : HotbarPresetEntry
{
    @JvmStatic
    val UNIQUE_ID: UUID = UUID.randomUUID()

    init
    {
        HotbarEntryStore[UNIQUE_ID.toString()] = this
    }

    override fun uniqueId(): UUID = UNIQUE_ID
    override fun buildItemStack(player: Player) = null

    override fun onRightClick(player: Player) {}
}
