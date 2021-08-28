package com.solexgames.lemon.player.nametag

import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

class ModModeNametagProvider: NametagProvider("mod-mode", 50) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo? {
        return if (toRefresh.hasMetadata("mod-mode")) {
            createNametag("${CC.GRAY}[M] ", "")
        } else {
            null
        }
    }
}
