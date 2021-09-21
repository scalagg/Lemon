package gg.scala.lemon.player.visibility

import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.visibility.OverrideAction
import net.evilblock.cubed.visibility.OverrideHandler
import org.bukkit.entity.Player

/**
 * @author puugz, GrowlyX
 */
class StaffVisibilityOverrideHandler : OverrideHandler {

    override fun getAction(toRefresh: Player, refreshFor: Player): OverrideAction {
        val lemonPlayer = PlayerHandler.findPlayer(toRefresh).orElse(null)
        val lemonPlayerTarget = PlayerHandler.findPlayer(refreshFor).orElse(null)

        return if (lemonPlayer != null && lemonPlayerTarget != null && !lemonPlayer.getSetting("hiding-staff") && lemonPlayerTarget.hasPermission("lemon.staff")) {
            OverrideAction.SHOW
        } else OverrideAction.NEUTRAL
    }
}
