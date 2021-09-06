package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.redis.RedisMessage
import com.solexgames.redis.annotation.Subscription
import com.solexgames.redis.handler.JedisHandler
import com.solexgames.redis.json.JsonAppender
import org.bukkit.Bukkit
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

    @JvmStatic
    fun buildMessage(packet: String, message: Map<String, String>): RedisMessage {
        return RedisMessage(JsonAppender(packet).also {
            message.forEach { (key, value) ->
                it.put(key, value)
            }
        }.asJson)
    }
}
