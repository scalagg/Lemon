package com.solexgames.lemon.util

import java.util.*

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object SplitUtil {

    @JvmStatic
    fun splitUuid(uuid: UUID): String {
        return uuid.toString().substring(0, 8)
    }
}
