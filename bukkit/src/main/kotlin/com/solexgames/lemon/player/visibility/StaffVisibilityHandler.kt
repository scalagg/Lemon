package com.solexgames.lemon.player.visibility

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.visibility.VisibilityAction
import net.evilblock.cubed.visibility.VisibilityAdapter
import org.bukkit.entity.Player


/**
 * @author GrowlyX
 * @since 9/7/2021
 */
class StaffVisibilityHandler : VisibilityAdapter {

    override fun getAction(toRefresh: Player, refreshFor: Player): VisibilityAction {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(toRefresh).orElse(null)
        val lemonPlayerTarget = Lemon.instance.playerHandler.findPlayer(refreshFor).orElse(null)

        if (lemonPlayer.getSetting("hiding-staff") && lemonPlayerTarget.hasPermission("lemon.staff")) {
            return VisibilityAction.HIDE
        }

        if (toRefresh.hasMetadata("vanished")) {
            if (!refreshFor.hasPermission("lemon.staff") || lemonPlayerTarget.activeGrant.getRank().weight < toRefresh.getMetadata("vanish-power")[0].asInt()) {
                return VisibilityAction.HIDE
            }
        }

        return VisibilityAction.NEUTRAL
    }
}
