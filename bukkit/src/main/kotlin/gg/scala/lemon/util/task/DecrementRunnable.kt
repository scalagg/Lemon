package gg.scala.lemon.util.task

import net.evilblock.cubed.util.Color
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author puugz
 * @since 23/08/2021 19:19
 */
abstract class DecrementRunnable(var seconds: Int): BukkitRunnable() {

    override fun run() {
        seconds--

        if (getSeconds().contains(seconds)) {
            onRun()
        } else if (seconds == 0) {
            onEnd()
            cancel()
        }
    }

    fun broadcast(message: String) {
        Bukkit.broadcastMessage(Color.translate(message))
    }

    abstract fun onRun()

    abstract fun onEnd()

    abstract fun getSeconds(): List<Int>

}
