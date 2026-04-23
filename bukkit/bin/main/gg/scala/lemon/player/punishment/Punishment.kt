package gg.scala.lemon.player.punishment

import gg.scala.common.Savable
import gg.scala.commons.annotations.Model
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.player.punishment.category.PunishmentCategoryIntensity
import gg.scala.lemon.util.other.Expirable
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

@Model
class Punishment(
    val uuid: UUID,
    @Indexed
    val target: UUID,
    val targetCurrentIp: String?,
    @Indexed
    val addedBy: UUID?,
    addedAt: Long,
    val addedOn: String,
    val addedReason: String,
    duration: Long,
    val category: PunishmentCategory
): Expirable(addedAt, duration), Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uuid

    var removedReason: String? = null
    var removedOn: String? = null
    @Indexed
    var removedBy: UUID? = null
    var removedAt: Long = -1

    val isRemoved: Boolean
        get() = removedAt != -1L

    val isActive: Boolean
        get() = !isRemoved && !hasExpired

    override fun save(): CompletableFuture<Void> {
        return DataStoreObjectControllerCache.findNotNull<Punishment>()
            .save(this, DataStoreStorageType.MONGO)
    }

    fun isIntensity(intensity: PunishmentCategoryIntensity): Boolean {
        return category.intensity == intensity
    }
}
