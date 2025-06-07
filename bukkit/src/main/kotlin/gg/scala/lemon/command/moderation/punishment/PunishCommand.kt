package gg.scala.lemon.command.moderation.punishment

import gg.scala.commons.acf.annotation.*
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.menu.punishment.PunishmentMenu
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister
object PunishCommand : ScalaCommand() {
    @CommandAlias("punish|pun|modpunish")
    @CommandPermission("lemon.command.punish")
    @Syntax("<player> [reason]")
    @CommandCompletion("@players Cheating|Abuse|Toxicity")
    fun onPunish(
        sender: CommandSender,
        uuid: AsyncLemonPlayer,
        @Optional reason: String?
    ) {
        if (sender !is Player) {
            sender.sendMessage("${CC.RED}Only players can use this command.")
            return
        }
        uuid.validatePlayers(sender, true) {
            val targetName = it.name
            val targetUuid = it.uniqueId
            val menuReason = reason ?: "No reason provided"
            PunishmentMenu(
                targetUuid = targetUuid,
                targetName = targetName,
                reason = menuReason
            ).openMenu(sender)
        }
    }
} 