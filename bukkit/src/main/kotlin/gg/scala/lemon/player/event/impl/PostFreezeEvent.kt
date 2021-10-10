package gg.scala.lemon.player.event.impl

import gg.scala.lemon.player.event.LemonEvent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList

/**
 * @author GrowlyX
 * @since 10/10/2021
 */
class PostFreezeEvent(
    val player: Player
) : LemonEvent()
{
    companion object
    {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun ifNotCancelled(): () -> Unit
    {
        return {}
    }

    override fun getHandlers(): HandlerList
    {
        return handlerList
    }
}
