package gg.scala.lemon.filter.auditing

import java.util.Date

/**
 * @author GrowlyX
 * @since 8/17/2024
 */
data class BsonTimestamp(
    val value: Long
)
{
    fun toDate() = Date(value)
}
