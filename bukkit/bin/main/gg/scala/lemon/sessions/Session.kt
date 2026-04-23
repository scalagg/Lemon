package gg.scala.lemon.sessions

import gg.scala.common.Savable
import gg.scala.commons.annotations.Model
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*

/**
 * @author GrowlyX
 * @since 8/9/2024
 */
@Model
data class Session(
    override val identifier: UUID = UUID.randomUUID(),
    @Indexed
    val playerID: UUID,
    val timestamp: Long,
    var length: Long = 0L
) : IDataStoreObject, Savable
{
    fun updateLength()
    {
        length = System.currentTimeMillis() - timestamp
    }

    override fun save() = DataStoreObjectControllerCache
        .findNotNull<Session>()
        .save(this, DataStoreStorageType.MONGO)
}
