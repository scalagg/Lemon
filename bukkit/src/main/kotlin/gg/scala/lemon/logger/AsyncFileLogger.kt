package gg.scala.lemon.logger

import com.google.common.io.Files
import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.time.TimeUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Paths
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

    private lateinit var thread: Thread
    internal lateinit var logFile: File

    private var started = false

    internal val queue = LinkedList<T>()

    fun queueForUpdates(t: T) {
        if (started) queue.add(t)
    }

    fun start() {
        val logFolder = File(
            Lemon.instance.dataFolder,
            "/logs/${fileName.toLowerCase()}"
        )

        if (!logFolder.exists())
            logFolder.mkdirs()

        logFile = File(
            logFolder,
           "${FORMAT.format(Date())}.txt"
        )

        thread = AsyncFileLoggerThread(this)
        thread.start()

        started = true
    }

    abstract fun formatToString(t: T): String

}
