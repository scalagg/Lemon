package com.solexgames.lemon.util.quickaccess

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import java.util.*

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
fun reloadPlayer(uuid: UUID) {
    Bukkit.getPlayer(uuid)?.let {
        Lemon.instance.playerHandler.findPlayer(it).ifPresent { lemonPlayer ->
            NametagHandler.reloadPlayer(it)

            VisibilityHandler.updateAllTo(it)
            VisibilityHandler.updateToAll(it)

            lemonPlayer.recalculateGrants(
                shouldCalculateNow = true
            )
        }
    }
}
