package com.solexgames.lemon.command.management.manual

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.Single
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class InvalidateGrantCommand : BaseCommand() {

    @CommandAlias("invalidategrant|ig")
    fun onInvalidate(player: Player, uuid: UUID, @Single id: String) {

    }
}
