package com.solexgames.lemon.player.nametag

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.entity.Player

class ModModeNametagProvider: NametagProvider("mod-mode", 2) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(toRefresh).orElse(null)

        return createNametag("", "")

//        return if (lemonPlayer.hasMetadata("mod-mode")) {
//            createNametag("${CC.GRAY}[M] ", "")
//        } else {
//            null
//        }
    }
}
