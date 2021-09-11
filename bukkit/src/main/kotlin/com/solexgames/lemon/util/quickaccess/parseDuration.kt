package com.solexgames.lemon.util.quickaccess

import net.evilblock.cubed.util.time.DateUtil

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
fun parseDuration(duration: String): Long {
    return when (duration) {
        "perm", "permanent", "forever" -> Long.MAX_VALUE
        else -> {
            try {
                val date: Long = DateUtil.parseDateDiff(duration, false)

                if (date == -1L) {
                    Long.MAX_VALUE
                } else {
                    System.currentTimeMillis() - date
                }
            } catch (exception: Exception) {
                Long.MAX_VALUE
            }
        }
    }
}
