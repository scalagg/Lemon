package com.solexgames.lemon.board

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.LemonPlayer
import net.evilblock.cubed.scoreboard.ScoreboardOverride
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object ModmodeBoardProvider : ScoreboardOverride() {

    override fun getTitle(player: Player): String {
        return CC.B_PRI + "Staff Mode"
    }

    override fun getLines(board: LinkedList<String>, player: Player) {
        val lemonPlayer = Lemon.instance.playerHandler.getPlayer(player)

        lemonPlayer.ifPresent {
//            board.add("${CC.GRAY}${CC.S}--------------------")
//            board.add(getVanishStatus(it))
//            board.add("Channel: ${CC.PRI}${getChannel(it.channelType)}")
//            board.add("Players: ${CC.PRI}${Bukkit.getOnlinePlayers().size}")
//            board.add("TPS: ${CC.PRI}${RedisUtil.getTicksPerSecondFormatted()}")
//            board.add(" ")
//            board.add("${CC.PRI}${Lemon.instance.serverHandler.network.websiteLink}")
//            board.add(CC.GRAY + CC.S + "--------------------")
        }
    }

    override fun shouldOverride(player: Player): Boolean {
        val lemonPlayer = Lemon.instance.playerHandler.getPlayer(player)
        var shouldOverride = false

        lemonPlayer.ifPresent { shouldOverride = it.isStaffMode }

        return shouldOverride
    }

//    private fun getChannel(channelType: ChatChannelType): String {
//        return if (channelType == null) "Regular" else {
//            when (channelType) {
//                DEV -> "&3Developer"
//                STAFF -> "&bStaff"
//                HOST -> "&2Host"
//                MANAGER -> "&4Manager"
//                ADMIN -> "&cAdmin"
//                OWNER -> "&9Owner"
//                else -> "Regular"
//            }
//        }
//    }

    private fun getVanishStatus(lemonPlayer: LemonPlayer): String {
        val status = if (lemonPlayer.isVanished) CC.RED + "Hidden" else CC.GREEN + "Visible"
//        status += if (lemonPlayer.isHidingStaff) " (Hiding Staff)" else " (Showing Staff)"
        return status
    }
}