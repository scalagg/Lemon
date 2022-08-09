package gg.scala.lemon.command.moderation

import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
@AutoRegister
object AltsCommand : ScalaCommand()
{
    @CommandAlias("alts|ipreport")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.alts")
    fun onAlts(
        sender: Player,
        uuid: AsyncLemonPlayer
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(sender, false) { target ->
            PlayerHandler.fetchAlternateAccountsFor(target.uniqueId).join()
                .let {
                    if (it.isEmpty())
                    {
                        throw ConditionFailedException("No alts were found for ${target.getOriginalColoredName()}${CC.RED}.")
                    }

                    val finalMessage = FancyMessage()

                    it.forEach { lemonPlayer ->
                        val newName = getNewName(lemonPlayer)
                        val colored = lemonPlayer.getColoredName()
                        val hoverList = mutableListOf<String>()

                        val lastIpAddress = lemonPlayer.getMetadata("last-ip-address")?.asString() ?: ""
                        val targetLastIpAddress = target.getMetadata("last-ip-address")?.asString() ?: ""

                        val matchingIpInfo = lastIpAddress == targetLastIpAddress
                        val previouslyMatched = lemonPlayer.pastIpAddresses.contains(targetLastIpAddress)

                        hoverList.add("${CC.PRI}${CC.STRIKE_THROUGH}--------------------------")
                        hoverList.add(
                            "${CC.SEC}Last Seen: ${CC.PRI}${
                                TimeUtil.formatIntoFullCalendarString(
                                    Date(
                                        lemonPlayer.getMetadata("last-connection")?.asString()
                                            ?.toLong() ?: System.currentTimeMillis()
                                    )
                                )
                            }"
                        )
                        hoverList.add("${CC.SEC}Active Rank: ${lemonPlayer.activeGrant!!.getRank().getColoredName()}")
                        hoverList.add("${CC.PRI}${CC.STRIKE_THROUGH}--------------------------")

                        val playerName = lemonPlayer.getColoredName()

                        hoverList.add(
                            if (matchingIpInfo)
                            {
                                "${CC.GREEN}IP matching $playerName${CC.GREEN}."
                            } else if (previouslyMatched)
                            {
                                "${CC.GOLD}Previously matched $playerName${CC.GOLD}."
                            } else
                            {
                                "${CC.B_GOLD}*${CC.GOLD}Previously matched $playerName${CC.GOLD}."
                            }
                        )

                        lemonPlayer.sortedPunishments()
                            .forEach { entry ->
                                if (entry.value != null)
                                {
                                    val ipAddress = entry.value!!.targetCurrentIp

                                    if (ipAddress != null)
                                    {
                                        if (ipAddress != targetLastIpAddress)
                                        {
                                            hoverList.add("${entry.key.color}${entry.key.fancyVersion}${CC.RED} is not matching ${CC.WHITE}$playerName${CC.RED}.")
                                        } else
                                        {
                                            hoverList.add("${entry.key.color}${entry.key.fancyVersion}${CC.GREEN} is matching ${CC.WHITE}$playerName${CC.GREEN}.")
                                        }
                                    }
                                }
                            }

                        hoverList.add("${CC.PRI}${CC.STRIKE_THROUGH}--------------------------")
                        hoverList.add("${target.getOriginalColoredName()}'s ${CC.SEC}Current IP Info:")

                        addIpInfoToList(target, hoverList)

                        if (matchingIpInfo)
                        {
                            hoverList.add("")
                            hoverList.add("$colored's ${CC.SEC}Matching IP Info:")

                            addIpInfoToList(lemonPlayer, hoverList)
                        }

                        hoverList.add("${CC.PRI}${CC.STRIKE_THROUGH}--------------------------")

                        finalMessage
                            .withMessage("$newName${CC.WHITE}, ")
                            .andHoverOf(*hoverList.toTypedArray())
                    }

                    sender.sendMessage("${target.getOriginalColoredName()}'s${CC.SEC} Alternate Accounts ${CC.GRAY}(x${it.size}):")

                    val lastComponent = finalMessage.components[finalMessage.components.size - 1]

                    lastComponent.let { comp ->
                        comp.value = comp.value.substring(0, comp.value.length - 2)
                    }

                    finalMessage.sendToPlayer(sender)
                }
        }
    }

    private fun addIpInfoToList(lemonPlayer: LemonPlayer, hoverList: MutableList<String>)
    {
        hoverList.add(" ${CC.SEC}Logins: ${CC.WHITE}${lemonPlayer.pastLogins.size}")
        hoverList.add(
            " ${CC.SEC}First Login: ${CC.WHITE}${
                TimeUtil.formatIntoFullCalendarString(
                    Date(lemonPlayer.getMetadata("first-connection")?.asString()?.toLong() ?: 1L)
                )
            }"
        )
        hoverList.add(
            " ${CC.SEC}Last Login: ${CC.WHITE}${
                TimeUtil.formatIntoFullCalendarString(
                    Date(lemonPlayer.getMetadata("last-connection")?.asString()?.toLong() ?: 1L)
                )
            }"
        )

        val completePlaytime = lemonPlayer
            .pastLogins.values.sum()
        val completePlaytimeSessions = lemonPlayer
            .pastLogins.size

        hoverList.add("")
        hoverList.add(" ${CC.SEC}Total sessions: ${CC.WHITE}$completePlaytimeSessions")
        hoverList.add(
            " ${CC.SEC}Total playtime: ${CC.WHITE}${
                TimeUtil.formatIntoAbbreviatedString((completePlaytime / 1000).toInt())
            }"
        )
    }

    private fun getNewName(lemonPlayer: LemonPlayer): String
    {
        lemonPlayer.recalculatePunishments(nothing = true).join()
        lemonPlayer.recalculateGrants().join()

        val sortedPunishmentFirst = lemonPlayer
            .sortedPunishments().firstOrNull()

        if (sortedPunishmentFirst != null)
        {
            return "${sortedPunishmentFirst.key.color}${lemonPlayer.name}"
        }

        return if (lemonPlayer.bukkitPlayer != null)
        {
            "${CC.GREEN}${lemonPlayer.name}"
        } else
        {
            "${CC.RED}${lemonPlayer.name}"
        }
    }
}
