package com.solexgames.lemon.handler

import com.solexgames.lemon.task.ShutdownTask
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 23/08/2021 19:14
 */
class ShutdownHandler {

    private var shutdownTask: ShutdownTask? = null

    fun initiateShutdown(seconds: Int, initiator: Player) {
        if (shutdownTask != null) {
            initiator.sendMessage("${CC.RED}A server shutdown has already been initialized.")
            return
        }
        // staff alert
        shutdownTask = ShutdownTask(seconds)
    }

    fun cancelShutdown(stopper: Player) {
        if (shutdownTask == null) {
            stopper.sendMessage("${CC.RED}There is no scheduled shutdown currently running.")
            return
        }
        shutdownTask!!.cancel()
        shutdownTask = null
    }
}