package com.solexgames.lemon.util.quickaccess

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
@OptIn(ExperimentalStdlibApi::class)
fun sendStaffMessage(
    sender: Player,
    message: String,
    addServer: Boolean,
    messageType: MessageType
): CompletableFuture<Void> {
    return RedisHandler.buildMessage(
        "staff-message",
        buildMap {
            val lemonPlayer = Lemon.instance.playerHandler.findPlayer(sender).orElse(null)

            put("sender", sender.uniqueId.toString())
            put("sender-fancy", lemonPlayer.getColoredName())
            put("message", message)
            put("permission", "lemon.staff")
            put("messageType", messageType.name)

            put("server", Lemon.instance.settings.id)
            put("with-server", addServer.toString())
        }
    ).publishAsync()
}

fun messageType(name: String): MessageType {
    return MessageType.valueOf(name)
}

enum class MessageType {
    PLAYER_MESSAGE,
    NOTIFICATION
}
