package com.solexgames.lemon.nametag

import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

class VanishNametagProvider: NametagProvider("vanish", 1) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo {
        return createNametag(CC.GRAY + "[V] ", "")
    }
}
