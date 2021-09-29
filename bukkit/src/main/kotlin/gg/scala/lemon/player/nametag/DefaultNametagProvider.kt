package gg.scala.lemon.player.nametag

import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.entity.Player

class DefaultNametagProvider: NametagProvider("default", 10) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo {
        return createNametag(
            QuickAccess.realRank(toRefresh).color, ""
        )
    }
}
