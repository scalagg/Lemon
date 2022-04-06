package gg.scala.lemon.channel.channels.staff

import net.kyori.adventure.text.format.NamedTextColor

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
enum class StaffChatChannelType(
    val prefix: Char,
    val color: NamedTextColor
)
{
    STAFF('#', NamedTextColor.AQUA),
    ADMIN('@', NamedTextColor.RED),
    DEVELOPER('$', NamedTextColor.GREEN),
    MANAGER('!', NamedTextColor.BLUE)
}
