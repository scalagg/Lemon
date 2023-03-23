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
class ChatChannelBuilder
{
    companion object
    {
        @JvmStatic
        fun newBuilder(): ChatChannelBuilder
        {
            return ChatChannelBuilder()
        }
    }

    private lateinit var identifier: String
    private lateinit var formatter: (UUID, Player?, String, String, Rank) -> TextComponent

    private var composite: ChatChannelComposite? = null

    fun import(
        composite: ChatChannelComposite
    ): ChatChannelBuilder
    {
        return apply {
            this.composite = composite
        }
    }

    fun identifier(
        identifier: String
    ): ChatChannelBuilder
    {
        return apply {
            this.identifier = identifier
        }
    }

    fun format(
        lambda: (UUID, Player?, String, String, Rank) -> TextComponent
    ): ChatChannelBuilder
    {
        return apply {
            this.formatter = lambda
        }
    }

    fun compose(): ChatChannel
    {
        if (composite != null)
        {
            return ChatChannel(this.composite!!)
        }

        return ChatChannel(object : ChatChannelComposite
        {
            override fun identifier() = identifier

            override fun format(
                sender: UUID,
                receiver: Player?,
                message: String,
                server: String,
                rank: Rank
            ) = formatter
                .invoke(sender, receiver, message, server, rank)
        })
    }
}
