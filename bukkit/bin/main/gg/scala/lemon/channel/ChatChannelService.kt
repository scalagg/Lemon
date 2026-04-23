package gg.scala.lemon.channel

import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
@Service
object ChatChannelService
{
    private val channels =
        mutableListOf<ChatChannel>()

    lateinit var default: ChatChannel
    lateinit var audiences: BukkitAudiences

    @Configure
    fun configure()
    {
        audiences = BukkitAudiences
            .create(Lemon.instance)
    }

    @Close
    fun close()
    {
        audiences.close()
    }

    fun accessibleChannels(
        player: Player
    ): List<ChatChannel>
    {
        return this.channels
            .filter {
                it.permissionLambda.invoke(player)
            }
    }

    fun find(id: String): ChatChannel?
    {
        if (id.lowercase() == "default")
            return default

        return channels
            .firstOrNull {
                it.composite()
                    .identifier()
                    .equals(id, true)
            }
    }

    fun registerDefault(
        channel: ChatChannel
    )
    {
        this.default = channel
    }

    fun register(
        channel: ChatChannel
    )
    {
        this.channels += channel
    }

    fun findAppropriateChannel(
        player: Player, message: String
    ): ChatChannel
    {
        var match = this.channels
            .filter {
                (it.prefix || !it.override) && it
                    .permissionLambda.invoke(player)
            }
            .firstOrNull {
                if (it.prefix) it.prefixed(message) else true
            }
            ?: default

        this.channels
            .filter {
                it.override
            }
            .filter {
                it.overrideLambda.invoke(player)
            }
            .maxByOrNull {
                it.overridePriority
            }
            ?.let {
                match = it
            }

        return match
    }
}
