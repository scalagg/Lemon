package gg.scala.lemon.command.moderation

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/25/2021
 */
object StaffVisibilityCommand : BaseCommand() {

    @CommandPermission("lemon.staff")
    @CommandAlias("togglestaffvisibility|tsv")
    fun onToggleStaffVisibility(player: Player) {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (!lemonPlayer.getSetting("hiding-staff")) {
            lemonPlayer.updateOrAddMetadata(
                "hiding-staff",
                Metadata(true)
            )

            player.sendMessage("${CC.RED}You've now hidden staff.")
        } else {
            lemonPlayer.removeMetadata("hiding-staff")

            player.sendMessage("${CC.GREEN}You've now viewing staff.")
        }

        VisibilityHandler.updateAllTo(player)
    }
}
