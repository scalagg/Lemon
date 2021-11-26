package gg.scala.lemon.player.nametag

import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

object VanishNametagProvider: NametagProvider("vanish", 25) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo? {
        return if (toRefresh.hasMetadata("vanished")) {
            createNametag("${CC.GRAY}[V] ", "")
        } else null
    }
}
