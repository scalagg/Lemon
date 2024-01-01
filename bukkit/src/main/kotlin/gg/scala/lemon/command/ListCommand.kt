package gg.scala.lemon.command

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler.getCorrectedPlayerList
import gg.scala.lemon.handler.RankHandler.getSortedRankString
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
@AutoRegister
object ListCommand : ScalaCommand()
{
    data class PlayerList(
        val maxCount: Int,
        val sortedPlayerEntries: List<String>
    )

    private var playerListCompute = { sender: CommandSender ->
        PlayerList(
            maxCount = Bukkit.getMaxPlayers(),
            sortedPlayerEntries = getCorrectedPlayerList(sender)
                .map(LemonPlayer::getColoredName)
        )
    }

    fun supplyCustomPlayerList(compute: (CommandSender) -> PlayerList)
    {
        this.playerListCompute = compute
    }

    @CommandAlias("list|who")
    fun onList(sender: CommandSender)
    {
        val playerList = playerListCompute(sender)
        val isMoreThan350 = playerList.sortedPlayerEntries.size > 350

        val sortedPlayers = playerList.sortedPlayerEntries
            .take(350)
            .joinToString(
                separator = "${CC.WHITE}, ${CC.RESET}"
            )

        sender.sendMessage(getSortedRankString())
        sender.sendMessage(
            "(${playerList.sortedPlayerEntries.size}/${playerList.maxCount}): $sortedPlayers${
                if (isMoreThan350) " ${CC.I_GRAY}(...and ${playerList.sortedPlayerEntries.size - 350} more)" else ""
            }"
        )
    }
}
