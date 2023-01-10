package gg.scala.lemon.channel.channels

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.channel.ChatChannelBuilder
import gg.scala.lemon.channel.ChatChannelComposite
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.internal.ExtHookIns
import gg.scala.lemon.minequest
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
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

    var chatTagProvider = { _: Player -> Component.text("") }

    private var additionalPrefixProvider = { _: Player -> Component.text("") }
    private var hoverEventProvider: (Player, Player) -> HoverEvent<Component>? = { _, _ -> null }

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

    val serializer =
        LegacyComponentSerializer
            .legacySection()
            .toBuilder()
            .extractUrls()
            .build()

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

        val strippedPrefix = ChatColor
            .stripColor(rank.prefix)

        val strippedSuffix = ChatColor
            .stripColor(rank.suffix)

        val prefix =
            (if (strippedPrefix.isNotEmpty())
                "${rank.prefix} " else "") + (if (minequest())
                serializer.serialize(chatTag).removePrefix(" ") else "")

        val suffix = if (strippedSuffix.isNotEmpty())
            " ${rank.suffix}" else ""

        var composed = "$prefix${
            lemonPlayer.getColoredName()
        }$suffix"

        var chatColor = rank.color

        if (
            minequest() && rank.name == "Platinum"
        )
        {
            val current = lemonPlayer
                .getMetadata("platinum")
                ?.asString() ?: "default"

            chatColor = ExtHookIns.customColorMappingChatColor(current, lemonPlayer)
            composed = ExtHookIns.customColorMappingFormatted(current, lemonPlayer, suffix)
        }

        return additionalPrefixProvider
            .invoke(bukkitPlayer)
            .append(
                serializer.deserialize(composed)
            )
            .let {
                return@let if (!minequest())
                {
                    it
                        .append(chatTag)
                        .append(colonComponent)
                } else
                {
                    it.append(
                        serializer.deserialize("$chatColor: ")
                    )
                }
            }
            .append(
                serializer.deserialize(
                    if (minequest())
                    {
                        if (rank.uuid == RankHandler.getDefaultRank().uuid) "${CC.GRAY}$colored" else colored
                    } else
                    {
                        colored
                    }
                )
            )
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
