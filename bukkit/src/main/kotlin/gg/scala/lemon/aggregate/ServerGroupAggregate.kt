package gg.scala.lemon.aggregate

import gg.scala.commons.agnostic.sync.server.ServerContainer
import gg.scala.commons.agnostic.sync.server.impl.GameServer
import gg.scala.lemon.lease.LeaseDependency
import gg.scala.lemon.lease.LeaseStrategy
import gg.scala.lemon.lease.lease
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 7/9/2022
 */
data class ServerGroupAggregate(
    val groupId: String
) : Runnable
{
    private val groupServers = mutableListOf<GameServer>()

    enum class ServerStatus
    {
        OFFLINE, WHITELISTED, ONLINE
    }

    val prominentServerStatus by lease(
        strategy = LeaseStrategy.ExpiredEager,
        executor = ServerGroupAggregates,
        dependencies = listOf(groupId)
    ) {
        if (groupServers.size == 0)
        {
            return@lease ServerStatus.OFFLINE
        }

        val whitelisted = groupServers
            .count {
                it.toServerStatus() == ServerStatus.WHITELISTED
            }

        val public = groupServers
            .count {
                it.toServerStatus() == ServerStatus.ONLINE
            }

        listOf(
            whitelisted to ServerStatus.WHITELISTED,
            public to ServerStatus.ONLINE
        ).maxByOrNull {
            it.first
        }?.second ?: ServerStatus.ONLINE
    }

    val totalPlayerCount by lease(
        strategy = LeaseStrategy.ExpiredEager,
        executor = ServerGroupAggregates,
        dependencies = listOf(groupId)
    ) {
        this.groupServers.sumOf { it.getPlayersCount() ?: 0 }
    }

    val totalMaxPlayerCount by lease(
        strategy = LeaseStrategy.ExpiredEager,
        executor = ServerGroupAggregates,
        dependencies = listOf(groupId)
    ) {
        this.groupServers.sumOf { it.getMaxPlayers() ?: 0 }
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
            this, 0L, 500L, TimeUnit.MILLISECONDS
        )
    }

    override fun run()
    {
        val instances = ServerContainer
            .allServers<GameServer>()
            .filter {
                this.groupId in it.groups
            }

        this.groupServers.clear()
        this.groupServers += instances

        LeaseDependency.invalidate(groupId)
    }
}

private fun GameServer.toServerStatus(): ServerGroupAggregate.ServerStatus
{
    return if (getWhitelisted() == true) ServerGroupAggregate.ServerStatus.WHITELISTED else ServerGroupAggregate.ServerStatus.ONLINE
}
