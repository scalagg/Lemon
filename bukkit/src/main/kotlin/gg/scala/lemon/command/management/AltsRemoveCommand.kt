package gg.scala.lemon.command.management

import gg.scala.lemon.handler.DataStoreOrchestrator
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandCompletion
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 10/30/2021
 */
class AltsRemoveCommand : BaseCommand()
{
    @CommandAlias("removealts")
    @CommandPermission("lemon.command.removealts")
    @CommandCompletion("@all-players")
    fun onAltsRemove(sender: CommandSender, target: UUID)
    {
        sender.sendMessage("${CC.GRAY}Fetching alternate accounts...")

        PlayerHandler.fetchAlternateAccountsFor(target).thenAccept { accounts ->
            if (accounts.isEmpty())
            {
                sender.sendMessage("${CC.RED}No alternate accounts were found.")
                return@thenAccept
            }

            accounts.forEach {
                DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
                    .delete(it.uniqueId, DataStoreStorageType.MONGO)
            }

            sender.sendMessage("${CC.GREEN}Successfully removed ${CC.D_AQUA}${accounts.size}${CC.GREEN} alternate accounts from the database.")
        }
    }
}
