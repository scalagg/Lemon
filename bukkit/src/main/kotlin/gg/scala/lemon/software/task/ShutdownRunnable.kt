package gg.scala.lemon.software.task

import gg.scala.lemon.Lemon
import gg.scala.lemon.util.task.DiminutionRunnable
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks.sync
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Bukkit

class ShutdownRunnable(seconds: Int): DiminutionRunnable(seconds) {

    init {
        runTaskTimerAsynchronously(Lemon.instance, 0L, 20L)
    }

    override fun onRun() {
        broadcast("${CC.SEC}The server will be rebooting in ${CC.PRI}${TimeUtil.formatIntoDetailedString(seconds)}${CC.SEC}.")
    }

    override fun onEnd() {
        Lemon.instance.server.shutdown()
    }

    override fun cancel() {
        super.cancel()

        if (seconds > 1)
        {
            broadcast("${CC.RED}The scheduled server shutdown has been cancelled!")
        }
    }

    override fun getSeconds(): List<Int> {
        return listOf(18000, 14400, 10800, 7200, 3600, 2700, 1800, 900, 600, 300, 240, 180, 120, 60, 50, 40, 30, 15, 10, 5, 4, 3, 2, 1)
    }
}