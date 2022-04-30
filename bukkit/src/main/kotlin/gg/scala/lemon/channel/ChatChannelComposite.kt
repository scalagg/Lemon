package gg.scala.lemon.channel

import gg.scala.lemon.player.rank.Rank
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
interface ChatChannelComposite
{
    fun identifier(): String

    fun format(
        sender: UUID,
        receiver: Player,
        message: String,
        server: String,
        rank: Rank
    ): TextComponent
}
