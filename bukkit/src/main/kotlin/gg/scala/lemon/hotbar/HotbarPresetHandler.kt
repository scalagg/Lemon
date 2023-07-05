package gg.scala.lemon.hotbar

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import me.lucko.helper.Events
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent

/**
 * @author GrowlyX
 * @since 11/28/2021
 */
@Service
object HotbarPresetHandler
{
    private val trackedHotbars = mutableMapOf<String, HotbarPreset>()

    fun startTrackingHotbar(string: String, hotbarPreset: HotbarPreset)
    {
        trackedHotbars[string] = hotbarPreset
    }

    fun forget(string: String)
    {
        trackedHotbars.remove(string)
    }

    @Configure
    fun configure()
    {
        Events
            .subscribe(PlayerInteractEvent::class.java)
            .filter { it.action == Action.RIGHT_CLICK_AIR }
            .filter { it.item != null }
            .filter { ItemUtils.itemTagHasKey(it.item, "invokerc") }
            .handler { event ->
                val extractedItemTag = ItemUtils
                    .readItemTagKey(
                        event.item,
                        "invokerc"
                    )

                val substring = extractedItemTag
                    .toString()
                    .removeSurrounding("\"")

                HotbarEntryStore[substring]
                    ?.onRightClick(
                        player = event.player
                    )
            }
    }

    infix fun Player.apply(preset: HotbarPreset)
    {
        preset.applyToPlayer(player)
    }

}
