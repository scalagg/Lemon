package gg.scala.lemon.logger.impl

import gg.scala.lemon.logger.AsyncFileLogger

/**
 * Basic implementation of [AsyncFileLogger]
 * which does not modify with the provided string.
 *
 * @author GrowlyX
 * @since 10/1/2021
 */
open class StringAsyncFileLogger(
    loggerName: String,
    fileName: String
) : AsyncFileLogger<String>(
    loggerName, fileName
) {

    override fun formatToString(t: String): String = t

}
