package gg.scala.lemon.player.comment

import gg.scala.lemon.util.SplitUtil
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 10/7/2021
 */
data class Comment(
    val uniqueId: UUID,
    val issuer: UUID,
    val target: UUID,
    val timestamp: Date,
    var value: String
) : IDataStoreObject
{
    override val identifier: UUID
        get() = uniqueId

    internal val shortenedUniqueId = SplitUtil.splitUuid(uniqueId)
}
