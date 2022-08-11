package gg.scala.lemon.util.minequest.trainer

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("PokeCraft")
object MinequestTrainerCooldowns : ProfileOrchestrator<MinequestTrainerCooldown>()
{
    @Configure
    fun configure()
    {
        this.subscribe()
    }

    fun getCooldown(
        uniqueId: UUID, trainer: String
    ): Long
    {
        return this.find(uniqueId)!!
            .cooldowns[trainer] ?: 0L
    }

    fun setCooldown(
        uniqueId: UUID, trainer: String
    )
    {
        val cooldown = this.find(uniqueId)
            ?: return

        cooldown.cooldowns[trainer] =
            System.currentTimeMillis()

        DataStoreObjectControllerCache
            .findNotNull<MinequestTrainerCooldown>()
            .save(cooldown, DataStoreStorageType.REDIS)
    }

    override fun new(uniqueId: UUID) = MinequestTrainerCooldown(uniqueId)
    override fun type() = MinequestTrainerCooldown::class
}
