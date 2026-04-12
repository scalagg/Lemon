package gg.scala.lemon.filter.auditing

import gg.scala.commons.annotations.Model
import gg.scala.lemon.serialization.BsonTimestamp
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

@Model
class MessageAuditLog(
    @Indexed val playerID: UUID,
    val message: String,
    override val identifier: UUID = UUID.randomUUID(),
    @Indexed val timestamp: BsonTimestamp = BsonTimestamp(System.currentTimeMillis())
) : IDataStoreObject
