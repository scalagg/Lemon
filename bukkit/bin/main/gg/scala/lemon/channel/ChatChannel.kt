package gg.scala.lemon.channel

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import java.util.UUID

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
open class ChatChannel(
    private val composite: ChatChannelComposite
)
{
    var distributed = false
    var distributionAllowFakeRanks = false
    var distributionGroupScoped = false
    var distributionGroup = ""

    var monitored = false

    var prefix = false
    var prefixCharacter = ' '

    var permissionLambda = { _: Player -> true }
    var displayToPlayer = { _: UUID, _: Player -> true }

    var override = false
    var overrideLambda = { _: Player -> true }
    var overridePriority = 0

    var usesRealRank = false

    fun forceRealRank(): ChatChannel
    {
        this.usesRealRank = true
        return this
    }

    fun sendToPlayer(
        player: Player,
        textComponent: TextComponent
    )
    {
        val audience = ChatChannelService
            .audiences.player(player)

        audience.sendMessage(textComponent)
    }

    fun sendToPlayerComponent(
        player: Player,
        component: Component
    )
    {
        val audience = ChatChannelService
            .audiences.player(player)

        audience.sendMessage(component)
    }

    fun useComposite(
        lambda: ChatChannelComposite.() -> Unit
    )
    {
        this.composite.lambda()
    }

    fun composite() = composite

    fun monitor(): ChatChannel
    {
        return apply { monitored = true }
    }

    fun distribute(): ChatChannel
    {
        return apply { distributed = true }
    }

    fun groupScopedDistribution(group: String) =
        apply {
            distributionGroupScoped = true
            distributionGroup = group
        }

    fun allowOnlyIf(
        lambda: (Player) -> Boolean
    ): ChatChannel
    {
        return apply {
            permissionLambda = lambda
        }
    }

    fun displayToPlayer(
        lambda: (UUID, Player) -> Boolean
    ): ChatChannel
    {
        return apply {
            displayToPlayer = lambda
        }
    }

    fun triggerPrefixed(
        character: Char
    ): ChatChannel
    {
        return apply {
            prefix = true
            prefixCharacter = character
        }
    }

    fun override(
        priority: Int,
        lambda: (Player) -> Boolean
    ): ChatChannel
    {
        return apply {
            override = true
            overrideLambda = lambda
            overridePriority = priority
        }
    }

    fun prefixed(
        message: String
    ): Boolean
    {
        return this.prefix && message.lowercase()
            .startsWith("$prefixCharacter ", true)
    }

    fun preFormat(
        message: String
    ): String
    {
        return if (this.prefix)
        {
            message.removePrefix(
                "${this.prefixCharacter} "
            )
        } else
        {
            message
        }
    }
}
