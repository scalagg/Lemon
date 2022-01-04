package gg.scala.lemon.hotbar

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import me.lucko.helper.Events
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import java.util.*

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
        Events.subscribe(PlayerInteractEvent::class.java)
            .filter { it.action.name.contains("RIGHT") }
            .filter { it.item != null }
            .handler { event ->
                var matchingEntry: HotbarPresetEntry? = null

                trackedHotbars.forEach { tracked ->
                    val first = tracked.value.entries
                        .values.firstOrNull {
                            val stack = it.buildItemStack(player = event.player) ?: return@firstOrNull false

                            return@firstOrNull ItemUtils.isSimilar(event.item, stack)
                        }
                        ?: return@forEach

                    matchingEntry = first
                }

                if (matchingEntry == null)
                {
                    return@handler
                }

                matchingEntry!!.onRightClick(
                    player = event.player
                )
            }
    }
}

infix fun Player.apply(preset: HotbarPreset)
{
    preset.applyToPlayer(player)
}
