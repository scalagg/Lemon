package gg.scala.lemon.channel.channels

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.ChatChannelBuilder
import gg.scala.lemon.channel.ChatChannelComposite
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.QuickAccess.realRank
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
@Service
object DefaultChatChannel : ChatChannelComposite
{
    private val colonComponent =
        Component.text(": ")
            .color(NamedTextColor.WHITE)

    @Configure
    fun configure()
    {
        val channel = ChatChannelBuilder
            .newBuilder()
            .import(this)
            .compose()
            .monitor()

        ChatChannelService
            .registerDefault(channel)
    }

    private var chatTagProvider = { _: Player -> Component.text("") }
    private var additionalPrefixProvider = { _: Player -> Component.text("") }

    fun provideChatTag(
        lambda: (Player) -> TextComponent
    )
    {
        this.chatTagProvider = lambda
    }

    fun provideAdditionalPrefix(
        lambda: (Player) -> TextComponent
    )
    {
        this.additionalPrefixProvider = lambda
    }

    private val serializer =
        LegacyComponentSerializer.legacySection()

    override fun identifier() = "default"

    override fun format(
        sender: UUID,
        receiver: Player,
        message: String,
        server: String,
        rank: Rank
    ): TextComponent
    {
        val bukkitPlayer = Bukkit
            .getPlayer(sender)

        val lemonPlayer =
            PlayerHandler.find(sender)!!

        val chatTag = this
            .chatTagProvider
            .invoke(bukkitPlayer)

        val colored = applyColors(
            bukkitPlayer, message
        ).replace(
            receiver.name,
            "${CC.YELLOW}${receiver.name}${CC.RESET}"
        )

        val prefix = if (rank.prefix.isNotBlank())
            "${rank.prefix} " else ""

        val suffix = if (rank.suffix.isNotBlank())
            " ${rank.prefix}" else ""

        val composed = "$prefix${
            lemonPlayer.getColoredName()
        }$suffix"

        val composedComponent =
            additionalPrefixProvider
                .invoke(bukkitPlayer)
                .append(
                    serializer.deserialize(composed)
                )

        val coloredComponent =
            serializer.deserialize(colored)

        composedComponent.append(chatTag)
        composedComponent.append(colonComponent)
        composedComponent.append(coloredComponent)

        return composedComponent
    }

    private fun applyColors(
        player: Player, message: String
    ): String
    {
        if (
            !player.hasPermission(
                "lemon.chat.colors"
            )
        )
        {
            return message
        }

        return Color.translate(message)
    }
}