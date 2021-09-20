package gg.scala.lemon.handler

import gg.scala.lemon.task.ShutdownRunnable
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

class ServerHandler {

    var shutdownRunnable: ShutdownRunnable? = null

    fun initiateShutdown(initiator: Player, seconds: Int) {
        if (shutdownRunnable != null) {
            initiator.sendMessage("${CC.RED}A server shutdown has already been initialized.")
            return
        }

        shutdownRunnable = ShutdownRunnable(seconds)
    }

    fun cancelShutdown(stopper: Player) {
        if (shutdownRunnable == null) {
            stopper.sendMessage("${CC.RED}There is currently no scheduled shutdown.")
            return
        }
        shutdownRunnable!!.cancel()
        shutdownRunnable = null
    }
}
