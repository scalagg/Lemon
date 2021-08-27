package com.solexgames.lemon.handler

import com.solexgames.lemon.util.redis.RedisMessage
import com.solexgames.redis.handler.JedisHandler
import com.solexgames.redis.json.JsonAppender

object RedisHandler: JedisHandler {

    @JvmStatic
    fun buildMessage(packet: String, message: HashMap<String, String>): RedisMessage {
        return RedisMessage(JsonAppender(packet).also {
            message.forEach { (key, value) ->
                it.put(key, value)
            }
        }.asJson)
    }
}
