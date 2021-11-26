package gg.scala.lemon.command.management

import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.menu.punishment.PunishmentViewMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
class HistoryCommand : BaseCommand()
{

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("history|c|check|hist")
    @CommandPermission("lemon.command.history.punishments")
    fun onHistory(player: Player, uuid: UUID)
    {
        val name =
            CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.history.punishments.other"))
        {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        val colored = QuickAccess.coloredNameOrNull(name)

        if (colored == null)
        {
            QuickAccess.computeColoredName(uuid, name).thenAccept {
                handleHistoryMenu(player, uuid, it, name, HistoryViewType.TARGET_HIST)
            }
        } else
        {
            handleHistoryMenu(player, uuid, colored, name, HistoryViewType.TARGET_HIST)
        }
    }

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("staffhistory|staffhist|csp|cp")
    @CommandPermission("lemon.command.staffhistory.punishments")
    fun onStaffHistory(player: Player, uuid: UUID)
    {
        val name =
            CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.staffhistory.punishments.other"))
        {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        val colored = QuickAccess.coloredNameOrNull(name)

        if (colored == null)
        {
            QuickAccess.computeColoredName(uuid, name).thenAccept {
                handleHistoryMenu(player, uuid, it, name, HistoryViewType.STAFF_HIST)
            }
        } else
        {
            handleHistoryMenu(player, uuid, colored, name, HistoryViewType.STAFF_HIST)
        }
    }

    private fun handleHistoryMenu(player: Player, uuid: UUID, colored: String, original: String, type: HistoryViewType)
    {
        player.sendMessage(
            "${CC.SEC}Viewing ${CC.PRI}$colored's${CC.SEC}${
                if (type == HistoryViewType.STAFF_HIST)
                {
                    " staff"
                } else
                {
                    ""
                }
            } history..."
        )

        PunishmentHandler.fetchAllPunishmentsForTarget(uuid).thenAccept {
            if (it.isEmpty())
            {
                player.sendMessage(
                    "${CC.RED}No punishments found ${
                        if (type == HistoryViewType.STAFF_HIST)
                        {
                            "by"
                        } else
                        {
                            "for"
                        }
                    } ${CC.YELLOW}$original${CC.RED}."
                )
                return@thenAccept
            }

            handleStaffHistory(uuid, it, type, player)
        }
    }

    private fun handleStaffHistory(uuid: UUID, it: List<Punishment>, type: HistoryViewType, player: Player)
    {
        PunishmentHandler.fetchPunishmentsRemovedBy(uuid).thenAccept { removed ->
            PunishmentViewMenu(
                uuid, type, it, removed
            ).openMenu(player)
        }
    }
}
