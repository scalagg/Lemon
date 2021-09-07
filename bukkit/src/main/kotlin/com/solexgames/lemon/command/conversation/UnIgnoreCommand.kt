package com.solexgames.lemon.command.conversation

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.CubedCacheUtil
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.Optional
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.entity.Player
import java.util.*

/**
 * @author puugz
 * @since 29/08/2021 01:01
 */
class UnIgnoreCommand : BaseCommand() {

    @Syntax("[player]")
    @CommandAlias("unignore|unblock")
    @CommandCompletion("@players-uv")
    fun onIgnore(player: Player, @Optional uuid: UUID?) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player).orElse(null)

        if (uuid == null) {
            if (lemonPlayer.ignoring.isEmpty()) {
                player.sendMessage("${CC.RED}You're not ignoring anyone.")
                return
            }

            player.sendMessage("${CC.B_PRI}Ignore List ${CC.SEC}(${CC.PRI}${lemonPlayer.ignoring.size}${CC.SEC}):")
            player.sendMessage("")

            lemonPlayer.ignoring.forEach {
                val playerName = CubedCacheUtil.fetchName(it) ?: "N/A"
                player.sendMessage("${CC.SEC}${Constants.DOUBLE_ARROW_RIGHT} ${CC.PRI}$playerName")
            }
            return
        }

        if (!lemonPlayer.ignoring.contains(uuid)) {
            throw ConditionFailedException("You're not ignoring this player.")
        }

        val playerName = CubedCacheUtil.fetchName(uuid) ?: "N/A"
        lemonPlayer.ignoring.remove(uuid)

        player.sendMessage("${CC.SEC}Stopped ignoring ${CC.PRI}$playerName${CC.SEC}.")
    }
}
