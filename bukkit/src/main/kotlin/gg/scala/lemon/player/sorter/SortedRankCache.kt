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
                teamMappings[entry.key] = getNameFromInput(index)
            }
    }

    /**
     * This is a special method to sort nametags in
     * the tablist. It takes a priority and converts
     * it to an alphabetic representation to force a
     * specific sort.
     *
     * @param input the sort priority
     * @return the team name
     */
    private fun getNameFromInput(input: Int): String
    {
        if (input < 0) return "Z"
        val letter = ((input / 5) + 65).toChar()
        val repeat = input % 5 + 1
        val builder = StringBuilder()
        for (i in 0 until repeat)
        {
            builder.append(letter)
        }
        return builder.toString()
    }

}
