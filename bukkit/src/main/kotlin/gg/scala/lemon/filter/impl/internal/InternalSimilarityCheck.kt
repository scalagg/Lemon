package gg.scala.lemon.filter.impl.internal

import java.util.*

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
object InternalSimilarityCheck
{
    fun findSimilarityBetween(
        first: String, second: String
    ): Double
    {
        var longer = first
        var shorter = second

        if (first.length < second.length)
        {
            longer = second
            shorter = first
        }

        val longerLength = longer.length

        return if (longerLength == 0)
        {
            1.0
        } else (longerLength - editDistance(longer, shorter)) / longerLength.toDouble()
    }

    private fun editDistance(
        first: String, second: String
    ): Int
    {
        val s1 = first.lowercase()
        val s2 = second.lowercase()

        val costs = IntArray(s2.length + 1)

        for (i in 0..s1.length)
        {
            var lastValue = i

            for (j in 0..s2.length)
            {
                if (i == 0) costs[j] = j else
                {
                    if (j > 0)
                    {
                        var newValue = costs[j - 1]
                        if (s1[i - 1] != s2[j - 1]) newValue = newValue.coerceAtMost(lastValue).coerceAtMost(costs[j]) + 1

                        costs[j - 1] = lastValue
                        lastValue = newValue
                    }
                }
            }

            if (i > 0) costs[s2.length] = lastValue
        }

        return costs[s2.length]
    }
}
