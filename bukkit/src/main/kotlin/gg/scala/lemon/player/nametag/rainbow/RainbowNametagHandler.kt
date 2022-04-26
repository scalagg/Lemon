package gg.scala.lemon.player.nametag.rainbow

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.bukkit.Rainbow
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

/**
 * @author GrowlyX
 * @since 11/25/2021
 */
@Service(name = "rainbow-nt")
object RainbowNametagHandler : Runnable
{
    private var index = 0

    private val options = listOf(
        ChatColor.DARK_RED,
        ChatColor.RED,
        ChatColor.GOLD,
        ChatColor.YELLOW,
        ChatColor.GREEN,
        ChatColor.DARK_GREEN,
        ChatColor.AQUA,
        ChatColor.DARK_AQUA,
        ChatColor.BLUE,
        ChatColor.DARK_BLUE,
        ChatColor.DARK_PURPLE,
        ChatColor.LIGHT_PURPLE
    )

    var currentColor = options.first()

    override fun run() {
        index++

        if (index >= options.size) {
            index = 0
        }

        currentColor = options[index]

        nametagInfo = NametagProvider.createNametag(
            currentColor.toString(), ""
        )

        rainbowNametagEnabled.forEach {
            val bukkitPlayer = Bukkit.getPlayer(it)
                ?: return@forEach

            NametagHandler.reloadPlayer(bukkitPlayer)
        }
    }

    val rainbowNametagEnabled = mutableSetOf<UUID>()

    var nametagInfo = NametagProvider.createNametag(
        currentColor.toString(), ""
    )

    @Configure
    fun configure()
    {
        Schedulers.async().runRepeating(this, 0L, 20L)

        Events.subscribe(PlayerQuitEvent::class.java).handler {
            rainbowNametagEnabled.remove(it.player.uniqueId)
        }
    }
}
