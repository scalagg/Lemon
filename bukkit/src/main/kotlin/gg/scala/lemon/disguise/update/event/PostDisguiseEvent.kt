package gg.scala.lemon.disguise.update.event

import net.evilblock.cubed.event.PluginEvent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
class PostDisguiseEvent(
    val player: Player
) : PluginEvent() {

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}
