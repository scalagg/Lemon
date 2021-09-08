package com.solexgames.lemon.player.board

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.LemonPlayer
import net.evilblock.cubed.scoreboard.ScoreboardOverride
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object ModModeBoardProvider: ScoreboardOverride() {

    override fun getTitle(player: Player): String {
        return "${CC.B_PRI}Mod Mode"
    }

    override fun getLines(board: LinkedList<String>, player: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            board.add("${CC.GRAY}${CC.S}--------------------")
            board.add(getVanishStatus(it))
            board.add("Players: ${CC.PRI}${Bukkit.getOnlinePlayers().size} ${CC.WHITE}(${CC.PRI}${Lemon.instance.getLocalServerInstance().metaData["highest-player-count"]}${CC.WHITE})")
            board.add("Channel: ${CC.PRI}${"Regular"}")
            board.add("TPS: ${CC.PRI}${String.format("%.2f", Lemon.instance.getLocalServerInstance().ticksPerSecond)}")
            board.add("${CC.GRAY}${CC.S}--------------------")
        }
    }

    override fun shouldOverride(player: Player): Boolean {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(player)
        var shouldOverride = false

        lemonPlayer.ifPresent {
            shouldOverride = it.getSetting("mod-mode")
        }

        return shouldOverride
    }

    private fun getVanishStatus(lemonPlayer: LemonPlayer): String {
        val vanished = lemonPlayer.getPlayer().orElse(null).hasMetadata("vanished")
        val hidingStaff = lemonPlayer.getSetting("hiding-staff")

        var status = if (vanished) CC.RED + "Hidden" else CC.GREEN + "Visible"
        status += if (hidingStaff) " (Hiding Staff)" else " (Showing Staff)"

        return status
    }
}
