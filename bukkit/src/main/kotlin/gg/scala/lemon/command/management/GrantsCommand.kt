package gg.scala.lemon.command.management

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.handler.PunishmentHandler
import gg.scala.lemon.menu.grant.GrantViewMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.coloredNameOrNull
import gg.scala.lemon.util.QuickAccess.computeColoredName
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.ConditionFailedException
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Syntax
import gg.scala.lemon.throwAnyExceptions
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 9/9/2021
 */
@AutoRegister
object GrantsCommand : ScalaCommand()
{
    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grants")
    @CommandPermission("lemon.command.history.grants")
    fun onHistory(
        player: Player,
        uuid: AsyncLemonPlayer
    ): CompletableFuture<Void>
    {
        return uuid.validatePlayers(player, true) {
            if (it.uniqueId != player.uniqueId && !player.hasPermission("lemon.command.history.grants.other"))
            {
                throw ConditionFailedException(LemonConstants.NO_PERMISSION_SUB)
            }

            val name = CubedCacheUtil.fetchName(it.uniqueId)!!
            val colored = coloredNameOrNull(name, true)

            if (colored == null)
            {
                computeColoredName(it.uniqueId, name)
                    .thenAccept { newUsername ->
                        handleGrantMenu(
                            player, it.uniqueId, newUsername
                        )
                    }
            } else
            {
                handleGrantMenu(
                    player, it.uniqueId, colored
                )
            }
        }
    }

    private fun handleGrantMenu(
        player: Player, uuid: UUID, colored: String,
        type: HistoryViewType = HistoryViewType.TARGET_HIST
    )
    {
        player.sendMessage(
            "${CC.SEC}Viewing ${CC.PRI}$colored's${CC.SEC} ${
                if (type == HistoryViewType.STAFF_HIST)
                {
                    "issued grants"
                } else
                {
                    "grant"
                }
            } history..."
        )

        val completableFuture = if (type == HistoryViewType.TARGET_HIST)
        {
            GrantHandler.fetchGrantsFor(uuid)
        } else
        {
            GrantHandler.fetchGrantsByExecutor(uuid)
        }

        completableFuture
            .thenAccept { grants ->
                if (grants.isEmpty())
                {
                    player.sendMessage("${CC.RED}No grants found for ${CC.YELLOW}$colored${CC.RED}.")
                    return@thenAccept
                }

                GrantViewMenu(
                    uuid, type, grants, colored
                ).openMenu(player)
            }
            .throwAnyExceptions()
    }

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grantstaffhistory|grantstaffhist|gsh|gstaffhist")
    @CommandPermission("lemon.command.staffhistory.grants")
    fun onStaffHistory(player: Player, uuid: AsyncLemonPlayer): CompletableFuture<Void>
    {
        return uuid.validatePlayers(player, true) {
            if (it.uniqueId != player.uniqueId && !player.hasPermission("lemon.command.staffhistory.grants.other"))
            {
                throw ConditionFailedException(LemonConstants.NO_PERMISSION_SUB)
            }

            val name = CubedCacheUtil.fetchName(it.uniqueId)!!
            val colored = coloredNameOrNull(name, true)

            if (colored == null)
            {
                computeColoredName(it.uniqueId, name)
                    .thenAccept { newUsername ->
                        handleGrantMenu(
                            player, it.uniqueId, newUsername, HistoryViewType.STAFF_HIST
                        )
                    }
            } else
            {
                handleGrantMenu(
                    player, it.uniqueId, colored, HistoryViewType.STAFF_HIST
                )
            }
        }
    }

}
