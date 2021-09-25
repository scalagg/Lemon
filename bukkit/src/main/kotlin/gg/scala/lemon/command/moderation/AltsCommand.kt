package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.other.FancyMessage
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
class AltsCommand : BaseCommand() {

    @Syntax("<target>")
    @CommandAlias("alts")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.alts")
    fun onAlts(sender: Player, target: OnlinePlayer) {
        val targetLemon = PlayerHandler.findPlayer(target.player.uniqueId).orElse(null)

        PlayerHandler.fetchAlternateAccountsFor(target.player.uniqueId).thenAccept {
            if (it.isEmpty()) {
                sender.sendMessage("${CC.RED}${targetLemon.getColoredName()}${CC.RED} does not have any alts.")
                return@thenAccept
            }

            val finalMessage = FancyMessage()

            it.forEach { lemonPlayer ->
                val newName = getNewName(lemonPlayer)
                val hoverList = mutableListOf<String>()

                val lastIpAddress = lemonPlayer.getMetadata("last-ip-address")?.asString() ?: ""
                val targetLastIpAddress = targetLemon.getMetadata("last-ip-address")?.asString() ?: ""

                val matchingIpInfo = lastIpAddress == targetLastIpAddress
                val previouslyMatched = lemonPlayer.pastIpAddresses.contains(targetLastIpAddress)

                hoverList.add("${CC.GRAY}${CC.STRIKE_THROUGH}---------------------")
                hoverList.add("${CC.SEC}Last Seen: ${CC.PRI}${
                    TimeUtil.formatIntoFullCalendarString(
                        Date(lemonPlayer.getMetadata("last-connection")?.asString()?.toLong() ?: System.currentTimeMillis())
                    )
                }")
                hoverList.add("${CC.GRAY}${CC.STRIKE_THROUGH}---------------------")

                hoverList.add(
                    if (matchingIpInfo) {
                        "${CC.GREEN}Currently matching $newName${CC.GREEN}."
                    } else if (previouslyMatched) {
                        "${CC.GOLD}Previously matched $newName${CC.GOLD}."
                    } else  {
                        "${CC.GOLD}Currently not matching $newName${CC.GOLD}."
                    }
                )

                lemonPlayer.activePunishments.forEach { entry ->
                    if (entry.value != null) {
                        val ipAddress = entry.value!!.targetCurrentIp

                        if (ipAddress != null) {
                            if (ipAddress != targetLastIpAddress) {
                                hoverList.add("${entry.key.color}${entry.key.fancyVersion}${CC.RED} is not matching ${CC.WHITE}${targetLemon.getColoredName()}${CC.RED}.")
                            } else {
                                hoverList.add("${entry.key.color}${entry.key.fancyVersion}${CC.GREEN} is matching ${CC.WHITE}${targetLemon.getColoredName()}${CC.GREEN}.")
                            }
                        }
                    }
                }

                hoverList.add("${CC.GRAY}${CC.STRIKE_THROUGH}---------------------")
                hoverList.add("${targetLemon.getColoredName()}'s ${CC.SEC}Current IP Info:")
                hoverList.add(" ${CC.SEC}Logins: ${CC.WHITE}${targetLemon.pastLogins.size}")
                hoverList.add(" ${CC.SEC}First Login: ${CC.WHITE}${
                    TimeUtil.formatIntoFullCalendarString(
                        Date(targetLemon.getMetadata("first-connection")?.asString()?.toLong() ?: 1L)
                    )
                }")
                hoverList.add(" ${CC.SEC}Last Login: ${CC.WHITE}${
                    TimeUtil.formatIntoFullCalendarString(
                        Date(targetLemon.getMetadata("last-connection")?.asString()?.toLong() ?: 1L)
                    )
                }")

                if (matchingIpInfo) {
                    hoverList.add("")
                    hoverList.add("$newName's ${CC.SEC}Matching IP Info:")
                    hoverList.add(" ${CC.SEC}Logins: ${CC.WHITE}${lemonPlayer.pastLogins.size}")
                    hoverList.add(" ${CC.SEC}First Login: ${CC.WHITE}${
                        TimeUtil.formatIntoFullCalendarString(
                            Date(lemonPlayer.getMetadata("first-connection")?.asString()?.toLong() ?: 1L)
                        )
                    }")
                    hoverList.add(" ${CC.SEC}Last Login: ${CC.WHITE}${
                        TimeUtil.formatIntoFullCalendarString(
                            Date(lemonPlayer.getMetadata("last-connection")?.asString()?.toLong() ?: 1L)
                        )
                    }")
                }

                hoverList.add("${CC.GRAY}${CC.STRIKE_THROUGH}---------------------")

                finalMessage
                    .withMessage("$newName${CC.WHITE}, ")
                    .andHoverOf(*hoverList.toTypedArray())
            }

            sender.sendMessage("${coloredName(target.player)}'s${CC.SEC} Alternate Accounts ${CC.GRAY}(x${it.size}):")

            val lastComponent = finalMessage.components[finalMessage.components.size - 1]

            lastComponent.let { comp ->
                comp.value = comp.value.substring(0, comp.value.length - 2)
            }

            finalMessage.sendToPlayer(sender)
        }
    }

    private fun getNewName(lemonPlayer: LemonPlayer): String {
        lemonPlayer.recalculatePunishments(nothing = true)
        lemonPlayer.recalculateGrants()

        if (lemonPlayer.activePunishments[PunishmentCategory.BLACKLIST] != null) {
            return "${CC.D_RED}${lemonPlayer.name}"
        } else if (lemonPlayer.activePunishments[PunishmentCategory.BAN] != null) {
            return "${CC.RED}${lemonPlayer.name}"
        } else if (lemonPlayer.activePunishments[PunishmentCategory.MUTE] != null) {
            return "${CC.I_WHITE}${lemonPlayer.name}"
        }

        return if (lemonPlayer.bukkitPlayer != null) {
            "${CC.GREEN}${lemonPlayer.name}"
        } else {
            "${CC.RED}${lemonPlayer.name}"
        }
    }
}
