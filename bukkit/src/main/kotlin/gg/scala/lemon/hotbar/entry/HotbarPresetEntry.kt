package gg.scala.lemon.hotbar.entry

import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
interface HotbarPresetEntry
{
    fun uniqueId(): UUID
    fun buildItemStack(player: Player): ItemStack?

    fun finalizedItemStack(player: Player): ItemStack?
    {
        val built = buildItemStack(player)
            ?: return null

        if (!ItemUtils.itemTagHasKey(built, "lemon_uuid"))
        {
            ItemUtils.addToItemTag(
                itemStack = built, key = "lemon_uuid",
                value = uniqueId().toString(), preserve = true
            )
        }

        return built
    }

    fun onRightClick(player: Player)
}