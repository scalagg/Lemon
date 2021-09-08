package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.CubedCacheUtil
import com.solexgames.lemon.util.quickaccess.MessageType
import com.solexgames.lemon.util.quickaccess.messageType
import com.solexgames.lemon.util.redis.RedisMessage
import com.solexgames.redis.annotation.Subscription
import com.solexgames.redis.handler.JedisHandler
import com.solexgames.redis.json.JsonAppender
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object RedisHandler: JedisHandler {

    @Subscription(action = "channel-message")
    fun onChannelMessage(jsonAppender: JsonAppender) {
        val message = jsonAppender.getParam("message")
        val sender = jsonAppender.getParam("sender")

        val rank = Lemon.instance.rankHandler.findRank(
            UUID.fromString(jsonAppender.getParam("rank"))
        ).orElse(Lemon.instance.rankHandler.getDefaultRank())

        val channel = Lemon.instance.chatHandler.findChannel(jsonAppender.getParam("channel")) ?: return

        Bukkit.getOnlinePlayers().forEach {
            if (channel.hasPermission(it)) {
                it.sendMessage(channel.getFormatted(message, sender, rank, it))
            }
        }
    }

    @Subscription(action = "staff-message")
    fun onStaffMessage(jsonAppender: JsonAppender) {
        val message = jsonAppender.getParam("message")
        val permission = jsonAppender.getParam("permission")
        val server = jsonAppender.getParam("server")
        val fancySender = jsonAppender.getParam("sender-fancy")

        val withServer = jsonAppender.getParam("with-server")!!.toBoolean()

        val baseMessage = "${CC.AQUA}[S] ${if (withServer) "${CC.D_AQUA}[$server] " else ""}"

        when (
            messageType(jsonAppender.getParam("messageType"))
        ) {
            MessageType.PLAYER_MESSAGE -> {
                sendMessage("$baseMessage$fancySender${CC.WHITE}: ${CC.AQUA}$message") {
                    return@sendMessage it.hasPermission(permission)
                }
            }
            MessageType.NOTIFICATION -> {
                sendMessage("$baseMessage$message") {
                    return@sendMessage it.hasPermission(permission)
                }
            }
        }
    }

    @Subscription(action = "manual-server-group-update")
    fun onManualGroupUpdate(jsonAppender: JsonAppender) {

    }

    private fun sendMessage(message: String, permission: (Player) -> Boolean) {
        Bukkit.getOnlinePlayers().forEach {
            if (permission.invoke(it)) {
                it.sendMessage(message)
            }
        }
    }

    @JvmStatic
    fun buildMessage(packet: String, message: Map<String, String>): RedisMessage {
        return RedisMessage(JsonAppender(packet).also {
            message.forEach { (key, value) ->
                it.put(key, value)
            }
        }.asJson)
    }
}
