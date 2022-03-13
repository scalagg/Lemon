package gg.scala.lemon.redirection

import gg.scala.aware.conversation.messages.ConversationMessageResponse
import java.util.UUID

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
class PlayerRedirectMessageResponse(
    conversationUuid: UUID,
    val server: String,
    val allowed: Boolean,
    val allowedMessage: String,
    val empty: Boolean = false
) : ConversationMessageResponse(conversationUuid)
