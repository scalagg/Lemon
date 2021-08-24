package com.solexgames.lemon.handler

import com.solexgames.lemon.task.impl.ShutdownRunnable
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 23/08/2021 19:14
 */
object ShutdownHandler {

    private var shutdownRunnable: ShutdownRunnable? = null

    fun initiateShutdown(seconds: Int, initiator: Player) {
        if (shutdownRunnable != null) {
            initiator.sendMessage("${CC.RED}A server shutdown has already been initialized.")
            return
        }

        shutdownRunnable = ShutdownRunnable(seconds)
    }

    fun cancelShutdown(stopper: Player) {
        if (shutdownRunnable == null) {
            stopper.sendMessage("${CC.RED}There is no scheduled shutdown currently running.")
            return
        }
        shutdownRunnable!!.cancel()
        shutdownRunnable = null
    }
}
