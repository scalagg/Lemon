package gg.scala.lemon.command.management

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandCompletion
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.wrapper.AsyncLemonPlayer
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import org.bukkit.command.CommandSender
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/30/2021
 */
@AutoRegister
object AltsRemoveCommand : ScalaCommand()
{
    @CommandAlias("removealts|altsremove")
    @CommandPermission("lemon.command.removealts")
    @CommandCompletion("@players")
    fun onAltsRemove(
        sender: CommandSender,
        target: AsyncLemonPlayer
    ): CompletableFuture<Void>
    {
        sender.sendMessage("${CC.GRAY}Fetching alternate accounts...")

        return target.validatePlayers(sender, false) {
            PlayerHandler
                .fetchAlternateAccountsFor(it.uniqueId)
                .thenAccept { accounts ->
                    if (accounts.isEmpty())
                    {
                        sender.sendMessage("${CC.RED}No alternate accounts were found.")
                        return@thenAccept
                    }

                    accounts.forEach { other ->
                        DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
                            .delete(other.uniqueId, DataStoreStorageType.MONGO)
                    }

                    sender.sendMessage("${CC.GREEN}Successfully removed ${CC.D_AQUA}${accounts.size}${CC.GREEN} alternate accounts from the database.")
                }
                .join()
        }
    }
}
