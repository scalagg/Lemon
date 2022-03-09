package gg.scala.lemon.disguise.update.event

import gg.scala.lemon.disguise.information.DisguiseInfo
import net.evilblock.cubed.event.PluginEvent
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
class UnDisguiseEvent(
    val player: Player,
    val disguiseInfo: DisguiseInfo
) : PluginEvent(), Cancellable {

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(p0: Boolean) {
        cancelled = p0
    }
}
