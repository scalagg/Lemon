package gg.scala.lemon.player.nametag

import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.entity.Player

class DefaultNametagProvider: NametagProvider("default-fallback", 0) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo {
        val lemonPlayer = PlayerHandler.findPlayer(toRefresh).orElse(null)

        return createNametag(lemonPlayer.activeGrant!!.getRank().color, "")
    }
}
