package com.solexgames.lemon.player.nametag

import net.evilblock.cubed.nametag.NametagInfo
import net.evilblock.cubed.nametag.NametagProvider
import org.bukkit.entity.Player

class DefaultNametagProvider : NametagProvider("Default", 0) {

    override fun fetchNametag(toRefresh: Player, refreshFor: Player): NametagInfo {
//        val lemonPlayer = Lemon.instance.playerHandler.getPlayer(toRefresh)

//        return lemonPlayer.ifPresent(Consumer { player -> createNametag(player.activeGrant.rank, "") })

        return createNametag("", "")
    }
}