package gg.scala.lemon.command.moderation.punishment

import gg.scala.lemon.handler.PunishmentHandler
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/13/2021
 */
object WarnCommand : BaseCommand() {

    @CommandAlias("warn")
    @CommandCompletion("@all-players")
    @CommandPermission("lemon.command.warn")
    fun onWarn(sender: CommandSender, uuid: UUID, reason: String) {
        PunishmentHandler.handleWarning(sender, uuid, reason)
    }
}
