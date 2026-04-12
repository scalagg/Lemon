package gg.scala.lemon.util

import java.util.*

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
