package com.solexgames.lemon.util.other

import net.evilblock.cubed.util.time.TimeUtil
import java.util.*

/**
 * @author puugz, GrowlyX
 * @since 23/08/2021 18:32
 */

open class Expireable(
    open val addedAt: Long,
    open val duration: Long
) {

    var expireDate: Date = Date(addedAt + duration)

    fun isPermanent(): Boolean {
        return duration == Long.MAX_VALUE
    }

    fun getExpirationString(): String {
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
