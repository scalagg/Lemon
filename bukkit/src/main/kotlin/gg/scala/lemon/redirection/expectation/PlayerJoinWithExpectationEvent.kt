package gg.scala.lemon.redirection.expectation

import gg.scala.lemon.redirection.PlayerRedirectMessageResponse
import net.evilblock.cubed.event.PluginEvent
import java.util.UUID

class PlayerJoinWithExpectationEvent(
    val uniqueId: UUID,
    val response: PlayerRedirectMessageResponse
) : PluginEvent()
