package gg.scala.lemon.redirection.expectation

import gg.scala.lemon.redirection.PlayerRedirectMessageResponse
import net.evilblock.cubed.event.PluginEvent
import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/14/2022
 */
class PlayerJoinWithExpectationEvent(
    val uniqueId: UUID,
    val response: PlayerRedirectMessageResponse
) : PluginEvent()
