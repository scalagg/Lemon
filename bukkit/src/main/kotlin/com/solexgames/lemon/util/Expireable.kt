package com.solexgames.lemon.util

import net.evilblock.cubed.util.time.TimeUtil
import java.util.*

/**
 * @author puugz, GrowlyX
 * @since 23/08/2021 18:32
 */

open class Expireable(addedAt: Long, duration: Long) {

    open val addedAt: Long = addedAt
    open val duration: Long = duration

    var expireDate: Date = Date(addedAt + duration)

    fun isPermanent(): Boolean {
        return duration == Long.MAX_VALUE
    }

    fun getExpirationFancy(): String {
        return if (isPermanent()) {
            "Never"
        } else {
            TimeUtil.formatIntoDateString(expireDate)
        }
    }

    fun getDurationString(): String {
        return if (isPermanent()) {
            "Permanent"
        } else {
            TimeUtil.formatIntoDetailedString(duration.toInt())
        }
    }

    fun hasExpired(): Boolean {
        return !isPermanent() && System.currentTimeMillis() >= addedAt + duration
    }
}
