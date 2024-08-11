package gg.scala.lemon.filter.ml

import gg.scala.commons.annotations.Model
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 8/10/2024
 */
@Model
class IncubatorChatML(
    override val identifier: UUID,
    val message: String,
    val prediction: Double
) : IDataStoreObject
