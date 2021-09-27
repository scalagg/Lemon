package gg.scala.lemon.util

import java.util.*

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
object SplitUtil {

    @JvmStatic
    fun split(any: Any?, splitAt: String): List<String> {
        return any.toString().split(splitAt)
    }

    @JvmStatic
    fun splitUuid(uuid: UUID): String {
        return uuid.toString().substring(0, 8)
    }
}
