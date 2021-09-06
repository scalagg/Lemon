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
 * @since 29/08/2021 00:55
 */
class IgnoreCommand: BaseCommand() {

    @Syntax("[player]")
    @CommandAlias("ignore|block")
    @CommandCompletion("player-uv")
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
                player.sendMessage(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.PRI}$playerName")
            }

            return
        }

        val targetLemonPlayer = Lemon.instance.playerHandler.findPlayer(uuid)

        targetLemonPlayer.ifPresent {
            if (it.isStaff()) {
                throw ConditionFailedException("You can't ignore this player.")
            }
        }

        if (lemonPlayer.ignoring.contains(uuid)) {
            throw ConditionFailedException("You're already ignoring this player.")
        }

        val playerName = CubedCacheUtil.fetchName(uuid) ?: "N/A"
        lemonPlayer.ignoring.add(uuid)

        player.sendMessage("${CC.SEC}Started ignoring ${CC.PRI}$playerName${CC.SEC}.")
    }
}
