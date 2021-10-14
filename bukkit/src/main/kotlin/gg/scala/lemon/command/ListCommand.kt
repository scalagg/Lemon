package gg.scala.lemon.command

import gg.scala.lemon.handler.PlayerHandler.getCorrectedPlayerList
import gg.scala.lemon.handler.RankHandler.getSortedRankString
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
object ListCommand : BaseCommand() {

    @CommandAlias("list|who|online")
    fun onList(sender: CommandSender) {
        val sortedRanks = getSortedRankString()
        val correctedPlayerList = getCorrectedPlayerList(sender)
        val isMoreThan350 = correctedPlayerList.size > 350

        val sortedPlayers = correctedPlayerList.joinToString(
            separator = "${CC.WHITE}, "
        ) {
            it.getColoredName()
        }

        sender.sendMessage(
            arrayOf(
                sortedRanks,
                "(${correctedPlayerList.size}/${Bukkit.getMaxPlayers()}): $sortedPlayers"
            )
        )

        if (isMoreThan350) {
            sender.sendMessage("${CC.RED}(Only displaying first 350 players)")
        }
    }

}
