package gg.scala.lemon.channel

import gg.scala.commons.annotations.custom.CustomAnnotationProcessors
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.annotation.PlayerChatEventCheck
import org.bukkit.event.player.AsyncPlayerChatEvent

@Service
object ChatChannelCheckService
{
    val chatChecks = mutableListOf<(AsyncPlayerChatEvent) -> Pair<String, Boolean>>()

    @Configure
    fun configure()
    {
        CustomAnnotationProcessors
            .process<PlayerChatEventCheck> { obj ->
                this.chatChecks += {
                    obj.javaClass.methods[0].invoke(obj, it) as Pair<String, Boolean>
                }
            }
    }
}
