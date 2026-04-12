package gg.scala.lemon.redirection

import gg.scala.aware.conversation.messages.ConversationMessage
import gg.scala.commons.agnostic.sync.ServerSync
import java.util.UUID

class PlayerRedirectMessage(
    val player: UUID,
    val server: String,
    val parameters: Map<String, String>,
    val from: String = ServerSync.getLocalGameServer().id
) : ConversationMessage()
