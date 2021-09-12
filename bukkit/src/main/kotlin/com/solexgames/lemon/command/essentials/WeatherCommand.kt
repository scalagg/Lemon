package com.solexgames.lemon.command.essentials

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 10/09/2021 17:51
 */
class WeatherCommand : BaseCommand() {

    @CommandAlias("day")
    @CommandPermission("lemon.command.day")
    fun onDay(player: Player) {
        player.world.time = 6000L
        player.sendMessage("${CC.GREEN}It's day now.")
    }

    @CommandAlias("night")
    @CommandPermission("lemon.command.night")
    fun onNight(player: Player) {
        player.world.time = 18000L
        player.sendMessage("${CC.GREEN}It's night now.")
    }

    @CommandAlias("sunset")
    @CommandPermission("lemon.command.sunset")
    fun onSunset(player: Player) {
        player.world.time = 12000L
        player.sendMessage("${CC.GREEN}It's now sunset.")
    }

}