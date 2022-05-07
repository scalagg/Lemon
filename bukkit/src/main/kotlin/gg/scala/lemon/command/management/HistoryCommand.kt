package gg.scala.lemon.command.management

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.menu.punishment.PunishmentViewMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
@AutoRegister
object HistoryCommand : ScalaCommand()
{
    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("history|c|check|hist")
    @CommandPermission("lemon.command.history.punishments")
    fun onHistory(player: Player, uuid: AsyncLemonPlayer): CompletableFuture<Void>
    {
        return uuid.validatePlayers(player, true) {
            val name = CubedCacheUtil.fetchName(it.uniqueId)!!

            if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.history.punishments.other"))
            {
                player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
                return@validatePlayers
            }

            val colored = QuickAccess.coloredNameOrNull(name)

            if (colored == null)
            {
                QuickAccess.computeColoredName(it.uniqueId, name).thenAccept { newColored ->
                    handleHistoryMenu(player, it.uniqueId, newColored, name, HistoryViewType.TARGET_HIST)
                }
            } else
            {
                handleHistoryMenu(player, it.uniqueId, colored, name, HistoryViewType.TARGET_HIST)
            }
        }
    }

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("staffhistory|staffhist|csp|cp")
    @CommandPermission("lemon.command.staffhistory.punishments")
    fun onStaffHistory(player: Player, uuid: AsyncLemonPlayer): CompletableFuture<Void>
    {
        return uuid.validatePlayers(player, true) {
            val name = CubedCacheUtil.fetchName(it.uniqueId)!!

            if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.staffhistory.punishments.other"))
            {
                player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
                return@validatePlayers
            }

            val colored = QuickAccess.coloredNameOrNull(name)

            if (colored == null)
            {
                QuickAccess.computeColoredName(it.uniqueId, name).thenAccept { coloredName ->
                    handleHistoryMenu(player, it.uniqueId, coloredName, name, HistoryViewType.STAFF_HIST)
                }
            } else
            {
                handleHistoryMenu(player, it.uniqueId, colored, name, HistoryViewType.STAFF_HIST)
            }
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
            } punishment history..."
        )

        val completableFuture = if (type == HistoryViewType.TARGET_HIST)
        {
            PunishmentHandler.fetchAllPunishmentsForTarget(uuid)
        } else
        {
            PunishmentHandler.fetchAllPunishmentsByExecutor(uuid)
        }

        completableFuture.thenAccept {
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

            handleStaffHistory(uuid, it, type, player, colored)
        }
    }

    private fun handleStaffHistory(
        uuid: UUID, it: List<Punishment>,
        type: HistoryViewType, player: Player, colored: String
    )
    {
        PunishmentHandler.fetchPunishmentsRemovedBy(uuid).thenAccept { removed ->
            PunishmentViewMenu(
                uuid, type, it, removed, colored
            ).openMenu(player)
        }
    }
}
