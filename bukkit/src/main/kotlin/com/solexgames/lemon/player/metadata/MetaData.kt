package com.solexgames.lemon.player.metadata

class MetaData(private var value: Any) {

    fun asInt(): Int {
        return value as Int
    }

    fun asString(): String {
        return value as String
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
