package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.other.FancyMessage
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
class AltsCommand : BaseCommand() {

    @Syntax("<target>")
    @CommandAlias("alts")
    @CommandPermission("lemon.command.alts")
    fun onAlts(sender: Player, target: OnlinePlayer) {
        val targetLemon = PlayerHandler.findPlayer(target.player.uniqueId).orElse(null)

        PlayerHandler.fetchAlternateAccountsFor(target.player.uniqueId).thenAccept {
            val finalMessage = FancyMessage()

            it.forEach { lemonPlayer ->
                val newName = getNewName(lemonPlayer)
                val hoverList = mutableListOf<String>()

                val lastIpAddress = lemonPlayer.getMetadata("last-ip-address")?.asString() ?: ""
                val targetLastIpAddress = targetLemon.getMetadata("last-ip-address")?.asString() ?: ""

                val matchingIpInfo = lastIpAddress == targetLastIpAddress
                val previouslyMatched = lemonPlayer.pastIpAddresses.contains(targetLastIpAddress)

                hoverList.add("${CC.SEC}Name: ${CC.PRI}${lemonPlayer.getColoredName()}")
                hoverList.add("${CC.SEC}Last Seen: ${CC.PRI}${
                    TimeUtil.formatIntoDateString(
                        Date(lemonPlayer.getMetadata("last-connection")?.asLong() ?: System.currentTimeMillis())
                    )
                }")
                hoverList.add("")
                hoverList.add(
                    if (matchingIpInfo) {
                        "${CC.GREEN}Currently matching $newName${CC.GREEN}."
                    } else if (previouslyMatched) {
                        "${CC.GOLD}Previously matched $newName${CC.GOLD}."
                    } else  {
                        "${CC.GOLD}Currently not matching $newName${CC.GOLD}."
                    }
                )

                hoverList.add("")
                hoverList.add("$newName's ${CC.SEC}Current IP Info:")
                hoverList.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}Logins: ${CC.WHITE}${lemonPlayer.pastLogins.size}")
                hoverList.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}First Login: ${CC.WHITE}${
                    TimeUtil.formatIntoDateString(
                        Date(lemonPlayer.getMetadata("first-connection")?.asLong() ?: 1L)
                    )
                }")
                hoverList.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}Last Login: ${CC.WHITE}${
                    TimeUtil.formatIntoDateString(
                        Date(lemonPlayer.getMetadata("last-connection")?.asLong() ?: 1L)
                    )
                }")

                hoverList.add("")
                hoverList.add("${targetLemon.getColoredName()}'s ${CC.SEC}Current IP Info:")
                hoverList.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}Logins: ${CC.WHITE}${targetLemon.pastLogins.size}")
                hoverList.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}First Login: ${CC.WHITE}${
                    TimeUtil.formatIntoDateString(
                        Date(targetLemon.getMetadata("first-connection")?.asLong() ?: 1L)
                    )
                }")
                hoverList.add(" ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.SEC}Last Login: ${CC.WHITE}${
                    TimeUtil.formatIntoDateString(
                        Date(targetLemon.getMetadata("last-connection")?.asLong() ?: 1L)
                    )
                }")

                finalMessage
                    .withMessage("$newName${CC.WHITE}, ")
                    .andHoverOf(*hoverList.toTypedArray())
            }

            sender.sendMessage("${coloredName(target.player)}'s${CC.SEC} Alternate Accounts ${CC.GRAY}(x${it.size}):")
            finalMessage.sendToPlayer(sender)
        }
    }

    private fun getNewName(lemonPlayer: LemonPlayer): String {
        lemonPlayer.recalculatePunishments(nothing = true)
            .getNow(null)
        lemonPlayer.recalculateGrants()
            .getNow(null)

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
