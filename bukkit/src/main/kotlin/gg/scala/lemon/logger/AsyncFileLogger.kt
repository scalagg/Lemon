package gg.scala.lemon.logger

import gg.scala.lemon.Lemon
import net.evilblock.cubed.logging.LogFile
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author GrowlyX
 * @since 10/1/2021
 */
abstract class AsyncFileLogger<T>(
    val loggerName: String,
    private val fileName: String
) {

    companion object {
        @JvmStatic
        val FORMAT = SimpleDateFormat("yyyy-dd-MM_hh.mm.ss")
    }

    private var started = false

    lateinit var handle: LogFile
    lateinit var flusher: AsyncFileLoggerThread<T>

    fun queueForUpdates(t: T) {
        if (!started)
            return

        handle.commit(formatToString(t))
    }

    fun initialize() {
        val logFolder = File(
            Lemon.instance.dataFolder,
            "/logs/${fileName.lowercase()}"
        )

        if (!logFolder.exists())
            logFolder.mkdirs()

        val logFile = File(
            logFolder,
           "${FORMAT.format(Date())}.txt"
        )

        handle = LogFile(logFile)

        flusher = AsyncFileLoggerThread(this)
        flusher.start()

        started = true
    }

    abstract fun formatToString(t: T): String

}
