package gg.scala.lemon.filter.ml

import gg.scala.commons.annotations.Model
import gg.scala.store.controller.annotations.Indexed
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 8/10/2024
 */
@Model
class IncubatorChatMLV2(
    override val identifier: UUID,
    @Indexed val playerID: UUID,
    val message: String,
    val prediction: Double,
    @Indexed val timestamp: Long
) : IDataStoreObject
