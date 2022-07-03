package gg.scala.lemon.discovery

import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import me.lucko.helper.promise.ThreadContext

/**
 * @author GrowlyX
 * @since 7/2/2022
 */
@Service
object LemonDiscoveryService
{
    @Configure
    fun configure()
    {
        LemonDiscoveryClient.register(
            Lemon.instance.settings.id,
            Lemon.instance.settings.group
        )
    }

    @Close
    fun close()
    {
        LemonDiscoveryClient.discovery()
            .agentClient()
            .deregister(Lemon.instance.settings.id)
    }
}
