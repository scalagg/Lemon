package gg.scala.lemon.util.minequest.potion

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.commons.persist.ProfileOrchestrator
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import java.time.Duration
import java.util.*

/**
 * @author GrowlyX
 * @since 8/10/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("PokeCraft")
object MinequestPotionCooldowns : ProfileOrchestrator<MinequestPotionCooldown>()
{
    @Configure
    fun configure()
    {
        this.subscribe()
    }

    fun getExpiration(
        uniqueId: UUID, type: MinequestPotionType
    ): Long
    {
        return this.find(uniqueId)!!
            .cooldowns[type] ?: 0L
    }

    fun isCooldownActive(
        uniqueId: UUID, type: MinequestPotionType
    ): Boolean
    {
        val expiration = this
            .getExpiration(uniqueId, type)

        return expiration >= System.currentTimeMillis()
    }

    fun setExpiration(
        uniqueId: UUID, type: MinequestPotionType, duration: Duration
    )
    {
        val cooldown = this.find(uniqueId)
            ?: return

        cooldown.cooldowns[type] =
            System.currentTimeMillis() + duration.toMillis()

        DataStoreObjectControllerCache
            .findNotNull<MinequestPotionCooldown>()
            .save(cooldown, DataStoreStorageType.REDIS)
    }

    override fun new(uniqueId: UUID) = MinequestPotionCooldown(uniqueId)
    override fun type() = MinequestPotionCooldown::class
}