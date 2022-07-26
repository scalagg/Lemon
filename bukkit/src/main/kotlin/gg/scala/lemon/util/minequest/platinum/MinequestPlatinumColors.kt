package gg.scala.lemon.util.minequest.platinum

import org.bukkit.ChatColor

/**
 * @author GrowlyX
 * @since 7/25/2022
 */
object MinequestPlatinumColors : Map<String, MinequestPlatinumColor> by mapOf(
    "default" to MinequestPlatinumColor(
        paneColor = 0,
        menuPosition = 10,
        "Default",
        "樀樁樂樃樄樅樆樇",
        ChatColor.GRAY
    ),
    "yellow" to MinequestPlatinumColor(
        paneColor = 4,
        menuPosition = 12,
        "Yellow",
        "樐樑樒樓樔樕樖樗",
        ChatColor.YELLOW
    ),
    "green" to MinequestPlatinumColor(
        paneColor = 5,
        menuPosition = 14,
        "Green",
        "樠模樢樣樤樥樦樧",
        ChatColor.GREEN
    ),
    "cyan" to MinequestPlatinumColor(
        paneColor = 3,
        menuPosition = 16,
        "Cyan",
        "樰樱樲樳樴樵樶樷",
        ChatColor.AQUA
    )
)
