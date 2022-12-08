package gg.scala.lemon.player.metadata

import java.util.*

// TODO: 4/6/2022 rewrite in the future
class Metadata(private var value: Any?)
{

    fun asInt(): Int
    {
        return value as Int
    }

    fun asString(): String?
    {
        if (value == null)
        {
            return null
        }

        return value as String
    }

    fun asUuid(): UUID
    {
        return UUID.fromString(asString())
    }

    fun asBoolean(): Boolean
    {
        return value as Boolean
    }

    fun asLong(): Long
    {
        return value as Long
    }

    fun asDouble(): Double
    {
        return value as Double
    }

}
