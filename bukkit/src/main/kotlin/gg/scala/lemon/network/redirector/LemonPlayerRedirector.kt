package gg.scala.lemon.network.redirector

import me.lucko.helper.network.redirect.PlayerRedirector
import me.lucko.helper.profiles.Profile
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 1/17/2022
 */
object LemonPlayerRedirector : PlayerRedirector
{
    override fun redirectPlayer(
        serverId: String, profile: Profile
    )
    {
        BungeeUtil.sendToServer(
            Bukkit.getPlayer(profile.uniqueId), serverId
        )
    }
}
