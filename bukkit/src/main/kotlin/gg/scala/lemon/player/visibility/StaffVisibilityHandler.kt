package gg.scala.lemon.player.visibility

import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.visibility.VisibilityAction
import net.evilblock.cubed.visibility.VisibilityAdapter
import org.bukkit.entity.Player

/**
 * @author puugz, GrowlyX
 */
class StaffVisibilityHandler : VisibilityAdapter {

    override fun getAction(toRefresh: Player, refreshFor: Player): VisibilityAction {
        val lemonPlayer = PlayerHandler.findPlayer(toRefresh).orElse(null)
        val lemonPlayerTarget = PlayerHandler.findPlayer(refreshFor).orElse(null)

        if (lemonPlayer != null && lemonPlayer.getSetting("hiding-staff") && lemonPlayerTarget.hasPermission("lemon.staff")) {
            return VisibilityAction.HIDE
        }

        if (toRefresh.hasMetadata("vanished")) {
            if (!refreshFor.hasPermission("lemon.staff") || lemonPlayerTarget.activeGrant!!.getRank().weight <= toRefresh.getMetadata("vanish-power")[0].asInt()) {
                return VisibilityAction.HIDE
            }
        }

        return VisibilityAction.NEUTRAL
    }
}
