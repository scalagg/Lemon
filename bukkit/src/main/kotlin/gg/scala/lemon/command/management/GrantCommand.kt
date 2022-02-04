package gg.scala.lemon.command.management

import gg.scala.lemon.menu.grant.context.GrantRankContextMenu
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
class GrantCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grant|g|grantscope")
    @CommandPermission("lemon.command.grant")
    fun onGrant(player: Player, uuid: UUID) {
        val name = CubedCacheUtil.fetchName(uuid)!!

        QuickAccess.computeColoredName(uuid, name).thenAccept {
            player.sendMessage("${CC.SEC}Granting for ${CC.PRI}$it${CC.SEC}...")

            GrantRankContextMenu(uuid, name).openMenu(player)
        }
    }
}
