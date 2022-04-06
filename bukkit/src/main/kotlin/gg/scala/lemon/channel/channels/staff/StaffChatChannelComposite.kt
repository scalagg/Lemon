package gg.scala.lemon.channel.channels.staff

import gg.scala.lemon.channel.ChatChannelComposite
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.CubedCacheUtil
import net.evilblock.cubed.util.CC
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
class StaffChatChannelComposite(
    val type: StaffChatChannelType
): ChatChannelComposite
{
    override fun identifier() =
        type.name.lowercase()

    override fun format(
        sender: UUID,
        receiver: Player,
        message: String,
        server: String,
        rank: Rank
    ): TextComponent
    {
        val username = CubedCacheUtil
            .fetchName(sender)

        return Component
            .text("[${type.name[0]}] ")
            .color(type.color)
            .append(
                Component.text("[$server] ")
                    .color(NamedTextColor.DARK_AQUA)
            )
            .append(
                LegacyComponentSerializer.legacySection()
                    .deserialize(
                        "${rank.color}$username${CC.WHITE}: ${
                            CC.AQUA + message
                        }"
                    )
            )
    }
}
