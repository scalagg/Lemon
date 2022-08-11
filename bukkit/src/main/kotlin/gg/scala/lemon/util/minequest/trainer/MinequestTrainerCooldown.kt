package gg.scala.lemon.util.minequest.trainer

import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
class MinequestTrainerCooldown(
    override val identifier: UUID,
    val cooldowns: MutableMap<String, Long> = mutableMapOf()
) : IDataStoreObject
