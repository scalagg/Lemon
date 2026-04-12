package gg.scala.lemon.redirection

import gg.scala.aware.conversation.messages.ConversationMessageResponse
import java.util.UUID

class PlayerRedirectMessageResponse(
    conversationUuid: UUID,
    val server: String,
    val allowed: Boolean,
    val allowedMessage: String,
    val empty: Boolean = false,
    val parameters: Map<String, String> = mapOf()
) : ConversationMessageResponse(conversationUuid)
