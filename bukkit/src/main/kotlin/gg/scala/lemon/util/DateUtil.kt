package gg.scala.lemon.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    fun format(timestamp: Long): String {
        return sdf.format(Date(timestamp))
    }
} 