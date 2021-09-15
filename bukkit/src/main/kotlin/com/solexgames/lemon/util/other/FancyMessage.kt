package com.solexgames.lemon.util.other

import com.solexgames.lemon.util.QuickAccess
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/15/2021
 */
class FancyMessage {

    private val components = mutableListOf<TextComponent>()

    fun withMessage(vararg messages: String): FancyMessage {
        components.add(
            TextComponent(
                messages.joinToString(separator = "\n")
            )
        )

        return this
    }

    fun andHoverOf(vararg hover: String): FancyMessage {
        try {
            val latestComponent = components[components.size]

            latestComponent.hoverEvent = HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                ComponentBuilder(
                    hover.joinToString(separator = "\n")
                ).create()
            )

            return this
        } catch (exception: Exception) {
            throw InvalidComponentException("No component found to apply hover to")
        }
    }

    fun andCommandOf(action: ClickEvent.Action, command: String): FancyMessage {
        try {
            val latestComponent = components[components.size]
            latestComponent.clickEvent = ClickEvent(action, command)

            return this
        } catch (exception: Exception) {
            throw InvalidComponentException("No component found to apply command to")
        }
    }

    fun sendToPlayer(player: Player) {
        player.spigot().sendMessage(
            *components.toTypedArray()
        )
    }

    fun sendToPlayerGlobally(uuid: UUID): CompletableFuture<Void> {
        return QuickAccess.sendGlobalPlayerFancyMessage(this, uuid)
    }

    class InvalidComponentException(message: String) : RuntimeException(message)

}
