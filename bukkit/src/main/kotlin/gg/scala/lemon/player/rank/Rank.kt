package gg.scala.lemon.player.rank

import gg.scala.aware.thread.AwareThreadContext
import gg.scala.common.Savable
import gg.scala.commons.annotations.Model
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.internal.ExtHookIns
import gg.scala.lemon.metadata.NetworkMetadataDataSync
import gg.scala.lemon.scope.ServerScope
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.storable.IDataStoreObject
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import java.util.*
import java.util.concurrent.CompletableFuture

@Model
data class Rank(
    val uuid: UUID = UUID.randomUUID(),
    var name: String,
    var weight: Int = 0,

    var prefix: String = CC.GRAY,
    var suffix: String = CC.RESET,
    var color: String = CC.GRAY,

    var visible: Boolean = true,
    var grantable: Boolean? = true,

    var displayName: String? = null,

    val children: ArrayList<UUID> = ArrayList<UUID>(),
    val permissions: ArrayList<String> = ArrayList<String>(),

    var serverScopes: MutableList<ServerScope>? =
        mutableListOf()
) : Savable, IDataStoreObject
{
    override val identifier: UUID
        get() = uuid

    fun scopes() = serverScopes!!
    fun grantable() = grantable!!

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

    fun getColoredName(
        ignoreMinequest: Boolean = false
    ): String
    {
        if (
            NetworkMetadataDataSync.serverName() == "Minequest" &&
            !ignoreMinequest
        )
        {
            return ExtHookIns
                .customRankColoredName.invoke(this)
                ?: getColoredName(true)
        }

        return color + (displayName ?: name)
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
        return DataStoreObjectControllerCache.findNotNull<Rank>()
            .save(this, DataStoreStorageType.MONGO)
    }

    fun saveAndPushUpdatesGlobally(): CompletableFuture<Void>
    {
        crosscheck()

        return this.save()
            .thenRun {
                RedisHandler.buildMessage(
                    "rank-update",
                    "uniqueId" to uuid.toString()
                ).publish(AwareThreadContext.SYNC)
            }
    }
}
