package com.solexgames.lemon.util.expirable

import net.evilblock.cubed.util.time.TimeUtil

/**
 * @author puugz
 * @since 23/08/2021 18:32
 */
open class Expireable(addedAt: Long, duration: Long) {

    val addedAt: Long = 0
    val duration: Long = 0

    fun isPermanent(): Boolean {
        return duration == Long.MAX_VALUE
    }

//    fun getExpiresAt(): String {
//        return if (isPermanent()) { "Never" } else { "" }
//    }

    fun getDurationString(): String {
        return if (isPermanent()) { "Permanent" } else { TimeUtil.formatIntoDetailedString(duration.toInt()) }
    }

    fun hasExpired(): Boolean {
        return !isPermanent() && System.currentTimeMillis() >= addedAt + duration
    }
}