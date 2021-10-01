package gg.scala.lemon.logger.impl.`object`

import gg.scala.lemon.logger.impl.StringAsyncFileLogger

/**
 * @author GrowlyX
 * @since 10/1/2021
 */
object CommandAsyncFileLogger : StringAsyncFileLogger(
    "Commands", "commands"
)
