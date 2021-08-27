package com.solexgames.lemon.player.nametag

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

class VanishNametagProvider: NametagProvider("vanish", 25) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo? {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(toRefresh).orElse(null)

        return if (lemonPlayer.hasMetadata("vanished")) {
            createNametag("${CC.GRAY}[V] ", "")
        } else {
            null
        }
    }
}
