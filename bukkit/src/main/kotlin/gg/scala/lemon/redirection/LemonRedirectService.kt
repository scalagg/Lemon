package gg.scala.lemon.redirection

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.network.SyncLemonInstanceData
import me.lucko.helper.network.redirect.PlayerRedirector
import me.lucko.helper.network.redirect.RedirectSystem
import me.lucko.helper.profiles.Profile
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bungee.BungeeUtil
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 3/4/2022
 */
@Service
object LemonRedirectService : PlayerRedirector
{
    @Configure
    fun configure()
    {
        val system = RedirectSystem.create(
            Lemon.instance.messenger, SyncLemonInstanceData, this
        )
    }

    override fun redirectPlayer(
        server: String, profile: Profile
    )
    {
        val player = Bukkit
            .getPlayer(profile.uniqueId)
            ?: return

        player.sendMessage("${CC.SEC}Sending you to ${CC.PRI}$server${CC.SEC}...")

        BungeeUtil.sendToServer(player, server)
    }
}
