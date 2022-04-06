package gg.scala.lemon.channel

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
open class ChatChannel(
    val composite: ChatChannelComposite
)
{
    var distributed = false
    var monitored = false

    var prefix = false
    var prefixCharacter = ' '

    var permission = false
    var permissionLambda = { _: Player -> true }

    var override = false
    var overrideLambda = { _: Player -> true }
    var overridePriority = 0

    fun monitor(): ChatChannel
    {
        return apply { monitored = true }
    }

    fun distribute(): ChatChannel
    {
        return apply { distributed = true }
    }

    fun allowOnlyIf(
        lambda: (Player) -> Boolean
    ): ChatChannel
    {
        return apply {
            permission = true
            permissionLambda = lambda
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
}
