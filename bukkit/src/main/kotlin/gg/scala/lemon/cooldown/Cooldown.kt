package gg.scala.lemon.cooldown

import net.evilblock.cubed.util.time.TimeUtil
import org.apache.commons.lang.time.DurationFormatUtils

/**
 * @author GrowlyX
 * @since 10/15/2021
 */
abstract class Cooldown<T>
{
    private val expiringTimes = mutableMapOf<T, Long>()
    private val start = System.currentTimeMillis()

    open fun addOrOverride(t: T)
    {
        expiringTimes[t] = calculateExpiry(t)
    }

    open fun reset(t: T)
    {
        expiringTimes.remove(t)
    }

    fun isActive(t: T): Boolean
    {
        val expiry = fetchExpiry(t) ?: return false

        return System.currentTimeMillis() < expiry
    }

    fun getPassed(): Long
    {
        return System.currentTimeMillis() - start
    }

    fun getRemainingFormatted(t: T): String
    {
        return formatMilliseconds(fetchRemaining(t))
    }

    private fun formatMilliseconds(ms: Long): String
    {
        val seconds = ms / 1000.0
        return when
        {
            seconds < 1 -> "%.1fs".format(seconds)
            seconds < 60 -> "${seconds.toInt()}s"
            seconds < 3600 -> "${(seconds / 60).toInt()}m"
            else -> "${(seconds / 3600).toInt()}h"
        }
    }

    fun fetchRemaining(t: T): Long
    {
        val expiry = fetchExpiry(t) ?: return 0L

        return expiry - System.currentTimeMillis()
    }

    fun fetchExpiry(t: T): Long?
    {
        return expiringTimes[t]
    }

    protected fun calculateExpiry(t: T): Long
    {
        return System.currentTimeMillis() + durationFor(t)
    }

    abstract fun id(): String
    abstract fun durationFor(t: T): Long

}
