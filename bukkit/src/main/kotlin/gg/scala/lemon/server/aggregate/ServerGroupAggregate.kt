package gg.scala.lemon.server.aggregate

import gg.scala.lemon.handler.ServerHandler
import gg.scala.lemon.lease.LeaseDependency
import gg.scala.lemon.lease.LeaseStrategy
import gg.scala.lemon.lease.lease
import gg.scala.lemon.server.ServerInstance
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 7/9/2022
 */
data class ServerGroupAggregate(
    val groupId: String
) : Runnable
{
    private val groupServers = mutableListOf<ServerInstance>()

    val prominentServerStatus by lease(
        strategy = LeaseStrategy.ExpiredEager,
        executor = ServerGroupAggregates,
        dependencies = listOf(groupId)
    ) {
        if (groupServers.size == 0)
        {
            return@lease ServerInstance.ServerStatus.OFFLINE
        }

        val whitelisted = groupServers
            .count {
                it.toServerStatus() == ServerInstance.ServerStatus.WHITELISTED
            }

        val public = groupServers
            .count {
                it.toServerStatus() == ServerInstance.ServerStatus.ONLINE
            }

        listOf(
            whitelisted to ServerInstance.ServerStatus.WHITELISTED,
            public to ServerInstance.ServerStatus.ONLINE
        ).maxByOrNull {
            it.first
        }?.second ?: ServerInstance.ServerStatus.ONLINE
    }

    val totalPlayerCount by lease(
        strategy = LeaseStrategy.ExpiredEager,
        executor = ServerGroupAggregates,
        dependencies = listOf(groupId)
    ) {
        this.groupServers.map { it.onlinePlayers }
    }

    val totalMaxPlayerCount by lease(
        strategy = LeaseStrategy.ExpiredEager,
        executor = ServerGroupAggregates,
        dependencies = listOf(groupId)
    ) {
        this.groupServers.map { it.maxPlayers }
    }

    val totalServerCount by lease(
        strategy = LeaseStrategy.ExpiredEager,
        executor = ServerGroupAggregates,
        dependencies = listOf(groupId)
    ) {
        this.groupServers.count()
    }

    fun subscribeAggregateRefresh()
    {
        ServerGroupAggregates.scheduleAtFixedRate(
            this, 0L, 1L, TimeUnit.SECONDS
        )
    }

    override fun run()
    {
        val instances = ServerHandler
            .fetchOnlineServerInstancesByGroup(groupId)
            .join()

        this.groupServers.clear()
        this.groupServers += instances.values

        LeaseDependency.invalidate(groupId)
    }
}
