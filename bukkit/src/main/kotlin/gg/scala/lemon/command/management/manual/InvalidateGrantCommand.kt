package gg.scala.lemon.command.management.manual

import gg.scala.lemon.Lemon
import gg.scala.lemon.player.enums.InvalidationType
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
class InvalidateGrantCommand : BaseCommand() {

    @CommandAlias("invalidategrants")
    @CommandPermission("lemon.command.invalidategrants")
    fun onInvalidate(sender: CommandSender, invalidationType: InvalidationType, uuid: UUID) {
        val completableFuture = if (invalidationType == InvalidationType.ISSUED) {
            Lemon.instance.grantHandler.invalidateAllGrantsBy(uuid, sender)
        } else {
            Lemon.instance.grantHandler.invalidateAllGrantsFor(uuid, sender)
        }

        sender.sendMessage("${CC.SEC}Starting grant invalidation...")

        completableFuture.thenRun {
            sender.sendMessage("${CC.GREEN}Finished grant invalidation.")
        }
    }

}