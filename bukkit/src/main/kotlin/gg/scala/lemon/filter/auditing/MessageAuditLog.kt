package gg.scala.lemon.filter.auditing

import gg.scala.commons.annotations.Model
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 8/10/2024
 */
@Model
class MessageAuditLog(
    @Indexed val playerID: UUID,
    val message: String,
    override val identifier: UUID = UUID.randomUUID(),
    @Indexed val timestamp: BsonTimestamp = BsonTimestamp(System.currentTimeMillis())
) : IDataStoreObject
