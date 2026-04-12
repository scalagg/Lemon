package gg.scala.lemon.hotbar

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import me.lucko.helper.Events
import net.evilblock.cubed.util.bukkit.ItemUtils
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent
import net.evilblock.cubed.util.bukkit.Tasks

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
            .subscribe(PlayerInteractEvent::class.java, EventPriority.LOWEST)
            .filter {
                it.action == Action.RIGHT_CLICK_AIR ||
                    it.action == Action.RIGHT_CLICK_BLOCK
            }
            .filter { it.hasItem() }
            .filter { ItemUtils.itemTagHasKey(it.item, "invokerc") }
            .handler { event ->
                val heldSlot = event.player.inventory.heldItemSlot
                val original = event.item?.clone()

                event.isCancelled = true
                event.setUseItemInHand(Event.Result.DENY)
                event.setUseInteractedBlock(Event.Result.DENY)
                if (original != null && original.type.name == "GOAT_HORN")
                {
                    event.player.inventory.setItem(heldSlot, null)
                    kotlin.runCatching {
                        event.player.javaClass.getMethod("setCooldown", original.type.javaClass, Int::class.javaPrimitiveType)
                            .invoke(event.player, original.type, 0)
                    }
                }
                Tasks.sync {
                    if (original != null && original.type.name == "GOAT_HORN")
                    {
                        event.player.inventory.setItem(heldSlot, original)
                        kotlin.runCatching {
                            event.player.javaClass.getMethod("setCooldown", original.type.javaClass, Int::class.javaPrimitiveType)
                                .invoke(event.player, original.type, 0)
                        }
                    }
                    event.player.updateInventory()
                }

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
