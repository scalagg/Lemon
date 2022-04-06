package gg.scala.lemon.handler

import gg.scala.flavor.service.Service
import org.bukkit.event.player.AsyncPlayerChatEvent

@Service
object ChatHandler
{
    val chatChecks = mutableListOf<(AsyncPlayerChatEvent) -> Pair<String, Boolean>>()

    var chatMuted = false
    var slowChatTime = 0

    fun registerChatCheck(
        lambda: (AsyncPlayerChatEvent) -> Pair<String, Boolean>
    )
    {
        chatChecks.add(lambda)
    }
}
