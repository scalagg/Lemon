package gg.scala.lemon.redirection.expectation

import net.evilblock.cubed.event.PluginEvent
import java.util.UUID

class PlayerRedirectExpectationEvent(
    val uniqueId: UUID,
    val from: String,
    val parameters: Map<String, String> = emptyMap()
) : PluginEvent()
