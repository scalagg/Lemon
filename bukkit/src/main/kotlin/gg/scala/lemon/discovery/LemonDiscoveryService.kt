package gg.scala.lemon.discovery

import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon

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
        if (Lemon.instance.settings.consulEnabled)
        {
            LemonDiscoveryClient.register(
                Lemon.instance.settings.id,
                Lemon.instance.settings.group
            )
        }
    }

    @Close
    fun close()
    {
        if (Lemon.instance.settings.consulEnabled)
        {
            LemonDiscoveryClient.discovery()
                .agentClient()
                .deregister(Lemon.instance.settings.id)
        }
    }
}
