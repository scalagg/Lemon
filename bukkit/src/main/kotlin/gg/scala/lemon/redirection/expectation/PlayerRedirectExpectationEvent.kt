package gg.scala.lemon.redirection.expectation

import net.evilblock.cubed.event.PluginEvent
import java.util.UUID

/**
 * @author GrowlyX
 * @since 7/14/2022
 */
class PlayerRedirectExpectationEvent(
    val uniqueId: UUID, val from: String
) : PluginEvent()
