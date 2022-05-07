package gg.scala.lemon.command.moderation

import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.commons.acf.BaseCommand
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/25/2021
 */
@AutoRegister
object StaffVisibilityCommand : ScalaCommand()
{
    @CommandPermission("lemon.staff")
    @CommandAlias("togglestaffvisibility|tsv")
    fun onToggleStaffVisibility(player: Player)
    {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (!lemonPlayer.getSetting("hiding-staff"))
        {
            lemonPlayer.updateOrAddMetadata(
                "hiding-staff",
                Metadata(true)
            )

            player.sendMessage("${CC.RED}You're now hiding staff.")
        } else
        {
            lemonPlayer remove "hiding-staff"

            player.sendMessage("${CC.GREEN}You're now viewing staff.")
        }

        VisibilityHandler.updateAllTo(player)
    }
}
