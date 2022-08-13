package gg.scala.lemon.util.minequest.potion

import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
class MinequestPotionCooldown(
    override val identifier: UUID,
    val cooldowns: MutableMap<MinequestPotionType, Long> = mutableMapOf()
) : IDataStoreObject
