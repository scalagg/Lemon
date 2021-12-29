package gg.scala.lemon.filter.impl

import gg.scala.lemon.filter.ChatMessageFilter
import gg.scala.lemon.filter.impl.internal.InternalSimilarityCheck
import me.lucko.helper.Events
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
object RepetitiveMessageFilter : ChatMessageFilter
{
    @JvmStatic
    val MAX_SIMILARITY_FLAG = 0.800

    private val lastMessages = mutableMapOf<UUID, String>()

    override fun loadResources()
    {
        Events.subscribe(PlayerQuitEvent::class.java).handler {
            lastMessages.remove(it.player.uniqueId)
        }
    }

    override fun formDescription(player: Player) =
        "The message matched the last message the player sent."

    override fun isFiltered(
        player: Player, message: String
    ): Boolean
    {
        val lastMessage = lastMessages[player.uniqueId]
        lastMessages[player.uniqueId] = message

        return if (lastMessage != null)
        {
            val similarity = InternalSimilarityCheck
                .findSimilarityBetween(message, lastMessage)

            similarity >= MAX_SIMILARITY_FLAG
        } else false
    }
}
