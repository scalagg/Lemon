package gg.scala.lemon.handler

import gg.scala.lemon.task.ShutdownRunnable
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

object ServerHandler {

    var shutdownRunnable: ShutdownRunnable? = null

    fun initiateShutdown(initiator: Player, seconds: Int) {
        if (shutdownRunnable != null) {
            throw ConditionFailedException("A server shutdown has already been initialized.")
        }

        shutdownRunnable = ShutdownRunnable(seconds)
    }

    fun cancelShutdown(stopper: Player) {
        if (shutdownRunnable == null) {
            throw ConditionFailedException("There is currently no scheduled shutdown.")
        }

        shutdownRunnable!!.cancel()
        shutdownRunnable = null
    }
}
