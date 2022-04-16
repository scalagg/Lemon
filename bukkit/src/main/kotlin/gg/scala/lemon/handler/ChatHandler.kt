package gg.scala.lemon.handler

import gg.scala.commons.annotations.custom.CustomAnnotationProcessors
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.annotation.PlayerChatEventCheck
import org.bukkit.event.player.AsyncPlayerChatEvent

@Service
object ChatHandler
{
    val chatChecks = mutableListOf<(AsyncPlayerChatEvent) -> Pair<String, Boolean>>()

    var chatMuted = false
    var slowChatTime = 0

    @Configure
    fun configure()
    {
        CustomAnnotationProcessors.process<PlayerChatEventCheck> {
            this.registerChatCheck {
                it.javaClass.methods[0].invoke(it) as Pair<String, Boolean>
            }
        }
    }

    fun registerChatCheck(
        lambda: (AsyncPlayerChatEvent) -> Pair<String, Boolean>
    )
    {
        chatChecks.add(lambda)
    }
}
