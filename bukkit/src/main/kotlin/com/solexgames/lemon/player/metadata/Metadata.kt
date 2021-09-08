package com.solexgames.lemon.player.metadata

import java.util.*

class Metadata(private var value: Any?) {

    constructor() : this(null)

    fun asInt(): Int {
        return value as Int
    }

    fun asString(): String {
        return value as String
    }

    fun asUuid(): UUID {
        return UUID.fromString(asString())
    }

    fun asBoolean(): Boolean {
        return value as Boolean
    }

    fun asLong(): Long {
        return value as Long
    }

    fun asDouble(): Double {
        return value as Double
    }

}
