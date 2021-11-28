package gg.scala.lemon.hotbar.entry.impl

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
class StaticHotbarPresetEntry(
    itemBuilder: ItemBuilder
) : HotbarPresetEntry
{
    private val built = itemBuilder.build()
    private val uniqueId: UUID = UUID.randomUUID()

    var onClick: (Player) -> Unit = {}

    override fun uniqueId(): UUID = uniqueId
    override fun buildItemStack(player: Player) = built

    override fun onRightClick(player: Player) {
        onClick.invoke(player)
    }
}