package gg.scala.lemon.handler

import gg.scala.lemon.Lemon
import gg.scala.lemon.server.ServerInstance
import gg.scala.lemon.task.ShutdownRunnable
import net.evilblock.cubed.acf.ConditionFailedException
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
        return Lemon.instance.serverLayer.fetchEntryByKey(id)
    }

    fun fetchOnlineServerInstancesByGroup(group: String): CompletableFuture<Map<String, ServerInstance>>
    {
        return Lemon.instance.serverLayer.fetchAllEntries().thenApply {
            val mutableMap = mutableMapOf<String, ServerInstance>()

            it.forEach { (t, u) ->
                if (u.serverGroup.equals(group, true)) {
                    mutableMap[t] = u
                }
            }

            return@thenApply mutableMap
        }
    }
}
