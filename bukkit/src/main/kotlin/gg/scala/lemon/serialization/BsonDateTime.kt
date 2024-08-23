package gg.scala.lemon.serialization

import java.util.*

/**
 * @author GrowlyX
 * @since 1/16/2024
 */
data class BsonDateTime(val epochMillis: Long)
{
    fun toDate() = Date(epochMillis)
}
