package gg.scala.lemon.player.rank

import gg.scala.common.Savable
import gg.scala.lemon.handler.DataStoreOrchestrator
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.util.dispatchImmediately
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.util.CC
import java.util.*
import java.util.concurrent.CompletableFuture

class Rank

@JvmOverloads
constructor(
    val uuid: UUID = UUID.randomUUID(),
    var name: String
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uuid

    var weight: Int = 0

    var prefix: String = CC.GRAY
    var suffix: String = CC.RESET
    var color: String = CC.GRAY

    var visible = true

    val children = ArrayList<UUID>()
    val permissions = ArrayList<String>()

    /**
     * Validates all [Rank]'s
     * in the [children] list.
     */
    private fun crosscheck()
    {
        children.toList().forEach {
            if (RankHandler.findRank(it) == null)
            {
                children.remove(it)
            }
        }
    }

    fun getColoredName(): String
    {
        return color + name
    }

    /**
     * Returns a list with permissions from
     * all inherited ranks as well as the current rank
     */
    fun getCompoundedPermissions(): ArrayList<String>
    {
        val compoundedPermissions = ArrayList<String>()

        permissions.forEach {
            if (!compoundedPermissions.contains(it))
            {
                compoundedPermissions.add(it)
            }
        }

        children.forEach {
            RankHandler.findRank(it)?.let { rank ->
                rank.getCompoundedPermissions().forEach { permission ->
                    if (!compoundedPermissions.contains(permission))
                    {
                        compoundedPermissions.add(permission)
                    }
                }
            }
        }

        return compoundedPermissions
    }

    override fun save(): CompletableFuture<Void>
    {
        return DataStoreOrchestrator.rankLayer.saveEntry(uuid.toString(), this)
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void>
    {
        crosscheck()

        return this.save().thenApply {
            RedisHandler.buildMessage(
                "rank-update",
                hashMapOf(
                    "uniqueId" to uuid.toString()
                )
            ).dispatchImmediately()

            return@thenApply null
        }
    }
}
