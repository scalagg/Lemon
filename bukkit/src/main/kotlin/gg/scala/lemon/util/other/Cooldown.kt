package gg.scala.lemon.util.other

import net.evilblock.cubed.util.time.TimeUtil

class Cooldown(duration: Long) {

    private val start = System.currentTimeMillis()
    private val expiry = start + duration

    fun getTimeLeft(): String {
        return TimeUtil.formatIntoDetailedString(getRemaining().toInt())
    }

    fun getPassed(): Long {
        return System.currentTimeMillis() - start
    }

    fun getRemaining(): Long {
        return expiry - System.currentTimeMillis()
    }

    fun isActive(): Boolean {
        return System.currentTimeMillis() < expiry
    }
}
