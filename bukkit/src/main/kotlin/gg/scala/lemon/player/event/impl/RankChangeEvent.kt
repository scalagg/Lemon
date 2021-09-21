package gg.scala.lemon.player.event.impl

import gg.scala.lemon.player.event.LemonEvent
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import java.util.*

/**
 * @author GrowlyX
 * @since 9/21/2021
 */
class RankChangeEvent(
    val player: Player,
    val oldRank: UUID?,
    val newRank: UUID
) : LemonEvent() {

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }

    override fun ifNotCancelled(): () -> Unit {
        return {}
    }

    override fun getHandlers(): HandlerList {
        return handlerList
    }
}
