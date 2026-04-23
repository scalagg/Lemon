package gg.scala.lemon.sessions

import com.mongodb.client.model.Filters
import gg.scala.lemon.player.LemonPlayer
import gg.scala.store.controller.DataStoreObjectControllerCache
import java.util.*

/**
 * @author GrowlyX
 * @since 8/9/2024
 */
object SessionService
{
    fun createLocal(player: LemonPlayer): Session
    {
        return Session(
            playerID = player.uniqueId,
            timestamp = System.currentTimeMillis()
        )
    }

    fun loadSessions(player: UUID) = DataStoreObjectControllerCache.findNotNull<Session>()
        .mongo()
        .loadAllWithFilterSync(Filters.eq(
            "playerID", player.toString()
        ))
}
