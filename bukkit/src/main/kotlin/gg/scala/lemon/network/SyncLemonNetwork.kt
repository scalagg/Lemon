package gg.scala.lemon.network

import me.lucko.helper.messaging.InstanceData
import me.lucko.helper.network.AbstractNetwork
import me.lucko.helper.redis.plugin.HelperRedis

/**
 * @author GrowlyX
 * @since 1/16/2022
 */
class SyncLemonNetwork(
    redisMessenger: HelperRedis,
    instanceData: InstanceData
) : AbstractNetwork(
    redisMessenger, instanceData
)
