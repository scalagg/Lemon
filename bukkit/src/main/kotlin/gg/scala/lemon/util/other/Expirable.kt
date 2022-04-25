package gg.scala.lemon.util.other

import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.apache.commons.lang.time.DurationFormatUtils
import java.util.*

/**
 * @author puugz, GrowlyX
 * @since 23/08/2021 18:32
 */
open class Expirable(
    val addedAt: Long,
    val duration: Long
)
{
    @Transient
    var expireDate = Date(addedAt + duration)

    val isPermanent: Boolean
        get() = duration == Long.MAX_VALUE

    val expirationString: String
        get() = if (isPermanent)
        {
            "Never"
        } else
        {
            TimeUtil.formatIntoCalendarString(expireDate)
        }

    val durationString: String
        get() = if (isPermanent)
        {
            "Permanent"
        } else
        {
            DurationFormatUtils.formatDurationWords(
                duration, true, true
            )
        }

    val fancyDurationString: String
        get() = if (isPermanent)
        {
            "${CC.RED}not${CC.SEC} expire"
        } else
        {
            "expire in ${CC.PRI}${
                DurationFormatUtils.formatDurationWords(
                    duration, true, true
                )
            }"
        }

    val fancyDurationStringRaw: String
        get() = if (isPermanent)
        {
            "not expire"
        } else
        {
            "expire in ${
                DurationFormatUtils.formatDurationWords(
                    duration, true, true
                )
            }"
        }

    val fancyDurationFromNowStringRaw: String
        get() = if (isPermanent)
        {
            "not expire"
        } else
        {
            "expire in ${
                DurationFormatUtils.formatDurationWords(
                    expireDate.time - System.currentTimeMillis(), true, true
                )
            }"
        }

    val durationFromNowStringRaw: String
        get() = if (isPermanent)
        {
            "not expire"
        } else
        {
            DurationFormatUtils.formatDurationWords(
                expireDate.time - System.currentTimeMillis(), true, true
            )
        }

    val hasExpired: Boolean
        get() = !isPermanent && System.currentTimeMillis() >= addedAt + duration
}
