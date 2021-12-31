package gg.scala.lemon.player

import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * Contains fundamental information for a specific player which
 * will be used across a network utilizing Lemon.
 *
 * @author GrowlyX
 * @since 10/19/2021
 */
class FundamentalLemonPlayer(
    val uniqueId: UUID,
    val username: String
) : IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    var currentServer = ""
    var currentDisplayName = ""
    var currentRank = UUID.randomUUID()
}
