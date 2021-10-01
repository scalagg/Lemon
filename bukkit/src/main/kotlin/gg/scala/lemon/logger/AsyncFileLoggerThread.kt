package gg.scala.lemon.logger

import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * @author GrowlyX
 * @since 10/1/2021
 */
class AsyncFileLoggerThread<T>(
    private val asyncFileLogger: AsyncFileLogger<T>
) : Thread("Async File Logger - ${asyncFileLogger.loggerName}") {

    override fun run() {
        val nextElement = asyncFileLogger.queue.pollFirst()

        if (nextElement != null) {
            val stringType = asyncFileLogger.formatToString(nextElement) + "\r\n"

            java.nio.file.Files.write(
                Paths.get(asyncFileLogger.logFile.path),
                stringType.encodeToByteArray(),
                StandardOpenOption.APPEND
            );
        }

        sleep(500L)
    }
}
