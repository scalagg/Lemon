package gg.scala.lemon.command.moderation

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.FrozenPlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.coloredName
import gg.scala.lemon.util.QuickAccess.nameOrConsole
import gg.scala.lemon.util.QuickAccess.sendStaffMessage
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.acf.annotation.Syntax
import net.evilblock.cubed.acf.bukkit.contexts.OnlinePlayer
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
class FreezeCommand : BaseCommand() {

    @Syntax("<player>")
    @CommandAlias("freeze|fr|ss")
    @CommandPermission("lemon.command.freeze")
    fun onFreeze(sender: CommandSender, target: LemonPlayer) {
        val alreadyFrozen = target.bukkitPlayer!!.hasMetadata("frozen")

        if (alreadyFrozen) {
            sendStaffMessage(
                null, "${CC.AQUA}${nameOrConsole(sender)}${CC.D_AQUA} has unfrozen ${coloredName(target.bukkitPlayer!!)}${CC.D_AQUA}.",
                true, QuickAccess.MessageType.NOTIFICATION
            )

            sender.sendMessage("${CC.GREEN}You've unfrozen ${coloredName(target.bukkitPlayer!!)}${CC.GREEN}.")
            target.bukkitPlayer!!.removeMetadata("frozen", Lemon.instance)

            FrozenPlayerHandler.expirables.remove(target.bukkitPlayer!!.uniqueId)
        } else {
            sendStaffMessage(
                null, "${CC.AQUA}${nameOrConsole(sender)}${CC.D_AQUA} has frozen ${coloredName(target.bukkitPlayer!!)}${CC.D_AQUA}.",
                true, QuickAccess.MessageType.NOTIFICATION
            )

            sender.sendMessage("${CC.GREEN}You've frozen ${coloredName(target.bukkitPlayer!!)}${CC.GREEN}.")

            target.bukkitPlayer!!.setMetadata(
                "frozen",
                FixedMetadataValue(
                    Lemon.instance, true
                )
            )

            FrozenPlayerHandler.expirables[target.bukkitPlayer!!.uniqueId] = FrozenPlayerHandler.FrozenExpirable()
        }
    }
}
