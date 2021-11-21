package gg.scala.lemon.command.management

import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.menu.grant.GrantViewMenu
import gg.scala.lemon.player.enums.HistoryViewType
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.coloredNameOrNull
import gg.scala.lemon.util.QuickAccess.computeColoredName
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
 * @since 9/9/2021
 */
class GrantsCommand : BaseCommand()
{

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grants")
    @CommandPermission("lemon.command.history.grants")
    fun onHistory(player: Player, uuid: UUID)
    {
        val name =
            CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.history.grants.other"))
        {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        val colored = coloredNameOrNull(name)

        if (colored == null)
        {
            computeColoredName(uuid, name).thenAccept {
                handleGrantMenu(
                    player, uuid, it
                )
            }
        } else {
            handleGrantMenu(
                player, uuid, colored
            )
        }
    }

    private fun handleGrantMenu(
        player: Player, uuid: UUID, colored: String,
        type: HistoryViewType = HistoryViewType.TARGET_HIST
    )
    {
        player.sendMessage("${CC.SEC}Viewing ${CC.PRI}$colored's${CC.SEC} ${
            if (type == HistoryViewType.STAFF_HIST)
            {
                "issued grants"
            } else
            {
                "grants"
            }
        }...")

        GrantHandler.fetchGrantsFor(uuid).thenAccept { grants ->
            try
            {
                if (grants.isEmpty())
                {
                    player.sendMessage("${CC.RED}No grants found for ${CC.YELLOW}$colored${CC.RED}.")
                    return@thenAccept
                }

                GrantViewMenu(
                    uuid, type, grants
                ).openMenu(player)
            } catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grantstaffhistory|grantstaffhist|gsh|gstaffhist")
    @CommandPermission("lemon.command.staffhistory.grants")
    fun onStaffHistory(player: Player, uuid: UUID)
    {
        val name = CubedCacheUtil.fetchName(uuid)
            ?: throw ConditionFailedException("Could not find a player by that uuid.")

        if (!player.uniqueId.equals(uuid) && !player.hasPermission("lemon.command.staffhistory.grants.other"))
        {
            player.sendMessage(LemonConstants.NO_PERMISSION_SUB)
            return
        }

        val colored = coloredNameOrNull(name)

        if (colored == null)
        {
            computeColoredName(uuid, name).thenAccept {
                handleGrantMenu(
                    player, uuid, it, HistoryViewType.STAFF_HIST
                )
            }
        } else {
            handleGrantMenu(
                player, uuid, colored, HistoryViewType.STAFF_HIST
            )
        }
    }

}
