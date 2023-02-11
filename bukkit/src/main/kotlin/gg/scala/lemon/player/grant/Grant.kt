package gg.scala.lemon.player.grant

import gg.scala.aware.thread.AwareThreadContext
import gg.scala.common.Savable
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.Model
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.other.Expirable
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import java.util.*
import java.util.concurrent.CompletableFuture

@Model
class Grant(
    val uuid: UUID,
    var target: UUID,
    var rankId: UUID,
    var addedBy: UUID?,
    addedAt: Long,
    var addedOn: String,
    var addedReason: String,
    duration: Long
) : Expirable(addedAt, duration), Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uuid

    var scopes = mutableListOf("global")

    var removedReason: String? = null
    var removedOn: String? = null
    var removedBy: UUID? = null
    var removedAt: Long = -1

    val isRemoved: Boolean
        get() = removedAt != -1L

    val isActive: Boolean
        get() = !isRemoved && !hasExpired

    fun getRank(): Rank
    {
        return RankHandler.findRank(rankId) ?: RankHandler.getDefaultRank()
    }

    fun isCustomScope(): Boolean
    {
        if (scopes.size == 1 && scopes[0] == "global")
        {
            return false
        }

        return scopes.isNotEmpty()
    }

    /**
     * Check if this grant has a scope
     * which matches the current server
     */
    fun isApplicable(): Boolean
    {
        if (scopes.contains("global"))
        {
            return true
        }

        var applicable = false

        for (scope in scopes)
        {
            val serverScope = getRank().scopes()
                .firstOrNull {
                    it.group == scope
                }

            if (
                serverScope != null &&
                serverScope.individual
                    .contains(ServerSync.getLocalGameServer().id)
            )
            {
                applicable = true
                break
            }

            if (scope in ServerSync.getLocalGameServer().groups)
            {
                applicable = true
                break
            }
        }

        return applicable
    }

    fun canRemove(lemonPlayer: LemonPlayer): Boolean
    {
        return lemonPlayer.activeGrant!!.getRank().weight >= getRank().weight && !isRemoved && !isAutoGrant()
    }

    fun isAutoGrant(): Boolean
    {
        return addedReason == "Automatic (Lemon)" && addedBy == null
    }

    override fun save(): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache.findNotNull<Grant>()
            .save(this, DataStoreStorageType.MONGO)
            .thenRun {
                RedisHandler.buildMessage(
                    "recalculate-grants",
                    "target" to target.toString()
                ).publish(
                    context = AwareThreadContext.SYNC
                )
            }
    }
}
