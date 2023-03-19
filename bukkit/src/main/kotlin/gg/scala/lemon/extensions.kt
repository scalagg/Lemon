package gg.scala.lemon

import gg.scala.lemon.processor.SettingsConfigProcessor
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 7/23/2022
 */
fun minequest() = Lemon.instance.lemonWebData.serverName == "Minequest"
fun config() = Lemon.instance.config<SettingsConfigProcessor>()

fun <T> CompletableFuture<T>.throwAnyExceptions(value: T? = null): CompletableFuture<T> =
    exceptionally {
        it.printStackTrace()
        return@exceptionally value
    }
