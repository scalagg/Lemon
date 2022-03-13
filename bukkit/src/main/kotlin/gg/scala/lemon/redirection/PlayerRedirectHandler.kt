package gg.scala.lemon.redirection

import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
interface PlayerRedirectHandler
{
    fun process(message: PlayerRedirectMessage):
            CompletableFuture<PlayerRedirectionResponse>
}
