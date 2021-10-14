package gg.scala.lemon.command.management

import gg.scala.lemon.menu.grant.issue.GrantIssueRankMenu
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
object GrantCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grant|g|grantscope")
    @CommandPermission("lemon.command.grant")
    fun onGrant(player: Player, uuid: UUID) {
        val name = CubedCacheUtil.fetchName(uuid) ?: throw ConditionFailedException("Could not find a player by that uuid.")
        val colored = QuickAccess.coloredName(name)

        player.sendMessage("${CC.SEC}Granting for ${CC.PRI}${colored}${CC.SEC}...")

        GrantIssueRankMenu(uuid, name).openMenu(player)
    }
}
