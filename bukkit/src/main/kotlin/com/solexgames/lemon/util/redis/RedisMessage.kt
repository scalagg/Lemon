package com.solexgames.lemon.util.redis

import com.solexgames.lemon.Lemon
import java.util.concurrent.CompletableFuture

/**
 * @author puugz
 * @since 27/08/2021 21:27
 */
class RedisMessage(private val message: String) {

    fun publishAsync(): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            Lemon.instance.jedisManager.publish(message)
        }
    }

    fun publishAsync(channel: String): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            Lemon.instance.jedisManager.runCommand {
                it.publish(channel, message)
            }
        }
    }

    fun publishSync() {
        Lemon.instance.jedisManager.publish(message)
    }
}
