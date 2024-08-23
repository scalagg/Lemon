package gg.scala.lemon.filter.ml

import gg.scala.common.Savable
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.Model
import gg.scala.lemon.serialization.BsonDateTime
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*

/**
 * @author GrowlyX
 * @since 11/23/2023
 */
@Model
data class ChatMLPunishmentAudit(
    @Indexed
    val target: UUID,
    @Indexed
    val fromServer: String = ServerSync.getLocalGameServer().id,
    val prediction: Double,
    val chatContext: List<String>,
    val timestamp: BsonDateTime = BsonDateTime(System.currentTimeMillis()),
    override val identifier: UUID = UUID.randomUUID()
) : IDataStoreObject, Savable
{
    override fun save() = DataStoreObjectControllerCache
        .findNotNull<ChatMLPunishmentAudit>()
        .save(this, DataStoreStorageType.MONGO)
}
