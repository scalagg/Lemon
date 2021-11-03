package gg.scala.lemon.logger

/**
 * Flushes recent commits periodically.
 *
 * @author GrowlyX
 * @since 10/1/2021
 */
class AsyncFileLoggerThread<T>(
    private val asyncFileLogger: AsyncFileLogger<T>
) : Thread("Async File Logger - ${asyncFileLogger.loggerName}") {

    override fun run() {
        asyncFileLogger.handle.flush()

        sleep(1000L)
    }
}
