package com.solexgames.lemon.util.other

import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.apache.commons.lang.time.DurationFormatUtils
import java.util.*

/**
 * @author puugz, GrowlyX
 * @since 23/08/2021 18:32
 */

open class Expirable(
    val addedAt: Long,
    val duration: Long
) {

    var expireDate: Date = Date(addedAt + duration)

    private fun isPermanent(): Boolean {
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
            DurationFormatUtils.formatDurationWords(
                duration, true, true
            )
        }
    }

    fun getFancyDurationString(): String {
        return if (isPermanent()) {
            "${CC.RED}never${CC.SEC} expire"
        } else {
            "expire in ${CC.PRI}${DurationFormatUtils.formatDurationWords(
                duration, true, true
            )}"
        }
    }

    fun hasExpired(): Boolean {
        return !isPermanent() && System.currentTimeMillis() >= addedAt + duration
    }
}
