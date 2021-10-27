package gg.scala.lemon.player.grant

import gg.scala.common.Savable
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.DataStoreHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.other.Expirable
import net.evilblock.cubed.util.bukkit.Tasks
import java.util.*
import java.util.concurrent.CompletableFuture

class Grant(
    val uuid: UUID,
    var target: UUID,
    var rankId: UUID,
    var addedBy: UUID?,
    addedAt: Long,
    var addedOn: String,
    var addedReason: String,
    duration: Long
) : Expirable(addedAt, duration), Savable
{

    var scopes: MutableList<String> = mutableListOf("global")

    var removedReason: String? = null
    var removedOn: String? = null
    var removedBy: UUID? = null
    var removedAt: Long = -1
    var isRemoved = false

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
            return scopes.contains("global")
        }

        var boolean = false

        scopes.forEach {
            if (Lemon.instance.settings.id.equals(it, true))
            {
                boolean = true
                return@forEach
            }
        }

        return boolean
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
        Tasks.asyncDelayed(2L) {
            RedisHandler.buildMessage(
                "recalculate-grants",
                mutableMapOf<String, String>().also {
                    it["target"] = target.toString()
                }
            )
        }

        return DataStoreHandler.grantLayer.saveEntry(uuid.toString(), this)
    }
}
