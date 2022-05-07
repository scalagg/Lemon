package gg.scala.lemon.handler

import gg.scala.lemon.server.ServerInstance
import gg.scala.lemon.task.ShutdownRunnable
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.impl.RedisDataStoreStorageLayer
import gg.scala.store.storage.type.DataStoreStorageType
import gg.scala.commons.acf.ConditionFailedException
import java.util.concurrent.CompletableFuture

object ServerHandler
{
    var shutdownRunnable: ShutdownRunnable? = null

    fun initiateShutdown(seconds: Int)
    {
        if (shutdownRunnable != null)
        {
            throw ConditionFailedException("A server shutdown has already been initialized.")
        }

        shutdownRunnable = ShutdownRunnable(seconds)
    }

    fun cancelShutdown()
    {
        if (shutdownRunnable == null)
        {
            throw ConditionFailedException("There is currently no scheduled shutdown.")
        }

        shutdownRunnable!!.cancel()
        shutdownRunnable = null
    }

    fun fetchServerInstanceById(id: String) : CompletableFuture<ServerInstance?>
    {
        val controller = DataStoreObjectControllerCache
            .findNotNull<ServerInstance>()

        return controller.useLayerWithReturn<RedisDataStoreStorageLayer<ServerInstance>, CompletableFuture<ServerInstance?>>(
            DataStoreStorageType.REDIS
        ) {
            this.loadWithFilter {
                it.serverId.equals(id, true)
            }
        }
    }

    fun fetchOnlineServerInstancesByGroup(group: String): CompletableFuture<Map<String, ServerInstance>>
    {
        return DataStoreObjectControllerCache.findNotNull<ServerInstance>()
            .loadAll(DataStoreStorageType.REDIS)
            .thenApply {
                val mutableMap = mutableMapOf<String, ServerInstance>()

                it.forEach { (t, u) ->
                    if (u.serverGroup.equals(group, true)) {
                        mutableMap[t.toString()] = u
                    }
                }

                return@thenApply mutableMap
            }
    }
}
