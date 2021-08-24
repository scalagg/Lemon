package com.solexgames.lemon.util

import com.solexgames.lemon.LemonConstants
import net.evilblock.cubed.util.time.TimeUtil
import java.util.*

/**
 * @author puugz, GrowlyX
 * @since 23/08/2021 18:32
 */

open class Expireable(addedAt: Long, duration: Long) {

    val addedAt: Long = 0
    val duration: Long = 0

    var expireDate: Date

    init {
        expireDate = Date(addedAt + duration)
    }

    fun isPermanent(): Boolean {
        return duration == Long.MAX_VALUE
    }

    fun getExpirationFancy(): String {
        return if (isPermanent()) {
            "Never"
        } else {
            LemonConstants.FORMAT.format(expireDate)
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
