package gg.scala.lemon.logger.impl.`object`

import gg.scala.lemon.logger.impl.StringAsyncFileLogger
import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 10/1/2021
 */
object ChatAsyncFileLogger : StringAsyncFileLogger(
    "Public Chat", "public-chat"
) {

    override fun formatToString(t: String): String {
        // Stripping chat colors so none of the color
        // characters are shown in the log file.
        return ChatColor.stripColor(t)
    }

}
