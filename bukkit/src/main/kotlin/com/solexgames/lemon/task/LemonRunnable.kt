package com.solexgames.lemon.task

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.util.Color
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable

/**
 * @author puugz
 * @since 23/08/2021 19:19
 */
abstract class LemonRunnable(seconds: Int) : BukkitRunnable() {

    var seconds = seconds

    override fun run() {
        if (getSeconds().contains(seconds)) {
            execute()
            seconds++
        } else {
            executeEnd()
            cancel()
        }
    }

    fun broadcast(message: String) {
        Bukkit.broadcastMessage(Color.translate(message))
    }

    fun broadcast(message: String, permission: String) {
        Bukkit.broadcast(message, permission)
    }

    abstract fun execute()

    abstract fun executeEnd()

    abstract fun getSeconds(): List<Int>

}