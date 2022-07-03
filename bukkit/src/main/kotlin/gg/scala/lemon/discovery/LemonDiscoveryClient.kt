package gg.scala.lemon.discovery

import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.ImmutableRegistration
import com.orbitz.consul.model.agent.Registration
import gg.scala.lemon.Lemon
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 7/2/2022
 */
object LemonDiscoveryClient
{
    private val consul = Consul
        .builder()
        .withHostAndPort(
            HostAndPort.fromParts(
                Lemon.instance.settings.consulAddress,
                Lemon.instance.settings.consulPort
            )
        )
        .withReadTimeoutMillis(1000L)
        .build()!!

    fun discovery() = this.consul

    fun register(
        serviceId: String,
        serviceName: String
    ): Registration
    {
        val registration = ImmutableRegistration
            .builder()
            .id(serviceId)
            .name(serviceName)
            .check(Registration.RegCheck.ttl(2L))
            .meta(mapOf(
                "region" to Lemon.instance.settings.datacenter
            ))
            .address("127.0.0.1")
            .port(Bukkit.getPort())
            .build()

        this.consul.agentClient()
            .register(registration)

        return registration
    }
}
