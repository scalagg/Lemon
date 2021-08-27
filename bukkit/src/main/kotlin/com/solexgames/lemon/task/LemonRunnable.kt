package com.solexgames.lemon.task

import net.evilblock.cubed.util.Color
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author puugz
 * @since 23/08/2021 19:19
 */
abstract class LemonRunnable(var seconds: Int): BukkitRunnable() {

    override fun run() {
        if (getSeconds().contains(seconds)) {
            onRun()
            seconds++
        } else {
            onEnd()
            cancel()
        }
    }

    fun broadcast(message: String) {
        Bukkit.broadcastMessage(Color.translate(message))
    }

    fun broadcast(message: String, permission: String) {
        Bukkit.broadcast(message, permission)
    }

    abstract fun onRun()

    abstract fun onEnd()

    abstract fun getSeconds(): List<Int>

}
