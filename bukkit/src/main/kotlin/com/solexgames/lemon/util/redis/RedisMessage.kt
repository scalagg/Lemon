package com.solexgames.lemon.util.redis

import com.solexgames.lemon.Lemon
import java.util.concurrent.CompletableFuture

/**
 * @author puugz
 * @since 27/08/2021 21:27
 */
class RedisMessage(private val message: String) {

    fun publish(async: Boolean = true) {
        if (async) {
            CompletableFuture.runAsync { Lemon.instance.jedisManager.publish(message) }
        } else {
            Lemon.instance.jedisManager.publish(message)
        }
    }
}