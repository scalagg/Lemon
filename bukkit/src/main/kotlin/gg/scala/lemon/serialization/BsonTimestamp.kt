package gg.scala.lemon.serialization

import java.util.Date

data class BsonTimestamp(
    val value: Long
)
{
    fun toDate() = Date(value)
}
