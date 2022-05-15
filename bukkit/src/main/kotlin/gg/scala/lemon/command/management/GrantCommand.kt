package gg.scala.lemon.command.management

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.menu.grant.context.GrantRankContextMenu
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
 * @since 9/23/2021
 */
@AutoRegister
object GrantCommand : ScalaCommand()
{
    @Syntax("<player>")
    @CommandCompletion("@players")
    @CommandAlias("grant|g|grantscope")
    @CommandPermission("lemon.command.grant")
    fun onGrant(player: Player, uuid: AsyncLemonPlayer): CompletableFuture<Void>
    {
        return uuid.validatePlayers(player, ignoreEmpty = true) {
            val name = CubedCacheUtil.fetchName(it.uniqueId)!!

            QuickAccess.computeColoredName(it.uniqueId, name)
                .thenAccept { colored ->
                    player.sendMessage("${CC.SEC}Granting for ${CC.PRI}$colored${CC.SEC}...")

                    GrantRankContextMenu(it.uniqueId, name, colored).openMenu(player)
                }
        }
    }
}
