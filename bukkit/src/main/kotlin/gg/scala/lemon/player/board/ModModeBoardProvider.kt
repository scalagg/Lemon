package gg.scala.lemon.player.board

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.scoreboard.ScoreboardOverride
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object ModModeBoardProvider: ScoreboardOverride() {

    override fun getTitle(player: Player): String {
        return "${CC.B_PRI}Mod Mode"
    }

    override fun getLines(board: LinkedList<String>, player: Player) {
        val lemonPlayer = PlayerHandler.findPlayer(player)

        lemonPlayer.ifPresent {
            board.add("${CC.GRAY}${CC.S}-------------------")
            board.add(getVanishStatus(it))
            board.add("")
            board.add("Players: ${CC.PRI}${Bukkit.getOnlinePlayers().size} ${CC.GRAY}(${Lemon.instance.localInstance.metaData["highest-player-count"]})")
            board.add("Channel: ${CC.PRI}${it.getMetadata("channel")?.asString() ?: "${CC.PRI}Regular"}")
            board.add("Ping: ${CC.PRI}${MinecraftReflection.getPing(player)}ms")
            board.add("TPS: ${CC.PRI}${String.format("%.2f", Lemon.instance.localInstance.ticksPerSecond)}")
            board.add("${CC.GRAY}${CC.S}-------------------")
        }
    }

    override fun shouldOverride(player: Player): Boolean {
        return player.hasMetadata("mod-mode")
    }

    private fun getVanishStatus(lemonPlayer: LemonPlayer): String {
        val vanished = lemonPlayer.bukkitPlayer!!.hasMetadata("vanished")
        val hidingStaff = lemonPlayer.getSetting("hiding-staff")

        var status = if (vanished) CC.RED + "Hidden" else CC.GREEN + "Visible"
        status += if (hidingStaff) " (Hiding Staff)" else " (Showing Staff)"

        return status
    }
}
