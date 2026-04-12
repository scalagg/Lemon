package gg.scala.lemon.redirection

import java.util.concurrent.CompletableFuture

interface PlayerRedirectHandler
{
    fun process(message: PlayerRedirectMessage):
            CompletableFuture<PlayerRedirectionResponse>
}
