package gg.scala.lemon.player.spatial.tablist.events

import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Event fired when a player subscribes to tab synchronization.
 * This allows other plugins to cancel or modify the subscription behavior.
 *
 * @author Limo
 * @date 2/3/2026
 */
class TabSyncSubscribeEvent(
    val player: Player,
    val syncGroup: String
) : Event(), Cancellable
{
    private var cancelled = false

    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun getHandlers(): HandlerList = handlerList

    override fun isCancelled(): Boolean = cancelled

    override fun setCancelled(cancel: Boolean)
    {
        this.cancelled = cancel
    }
}
