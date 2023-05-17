package gg.scala.lemon.player.sorter

import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.lemon.handler.RankHandler
import me.lucko.helper.promise.ThreadContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author GrowlyX
 * @since 8/31/2022
 */
@Repeating(5L, context = ThreadContext.ASYNC)
object SortedRankCache : Runnable
{
    val teamMappings = ConcurrentHashMap<UUID, String>()

    override fun run()
    {
        RankHandler.ranks.entries
            .filter { it.value.visible }
            .sortedByDescending { it.value.weight }
            .forEachIndexed { index, entry ->
                teamMappings[entry.key] = (index).toString()
            }
    }
}
