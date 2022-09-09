package gg.scala.lemon.cooldown

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
        return DurationFormatUtils.formatDurationWords(
            fetchRemaining(t), true, true
        )
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
