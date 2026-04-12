package gg.scala.lemon.filter.ml

import gg.scala.commons.annotations.Model
import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

@Model
class IncubatorChatML(
    val message: String,
    val prediction: Double,
    override val identifier: UUID = UUID.randomUUID()
) : IDataStoreObject

