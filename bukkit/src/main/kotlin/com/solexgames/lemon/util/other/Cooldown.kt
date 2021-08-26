package com.solexgames.lemon.util.other

import net.evilblock.cubed.util.time.TimeUtil

class Cooldown(millis: Long) {

    private val expiry: Long = System.currentTimeMillis() + millis

    fun expirationFormat(): String {
        return TimeUtil.formatIntoDetailedString(expiry.toInt())
    }

    fun isActive(): Boolean {
        return System.currentTimeMillis() < expiry
    }
}
