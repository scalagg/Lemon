package gg.scala.lemon.serialization

import java.util.*

data class BsonDateTime(val epochMillis: Long)
{
    fun toDate() = Date(epochMillis)
}
