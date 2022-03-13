package gg.scala.lemon.redirection

import gg.scala.lemon.Lemon
import java.util.UUID

/**
 * @author GrowlyX
 * @since 3/13/2022
 */
class PlayerRedirectionResponse(
    private val allowed: Boolean,
    private val allowedMessage: String
)
{
    internal fun wrap(
        conversationId: UUID
    ): PlayerRedirectMessageResponse
    {
        return PlayerRedirectMessageResponse(
            conversationId, Lemon.instance.settings.id,
            allowed, allowedMessage
        )
    }
}
