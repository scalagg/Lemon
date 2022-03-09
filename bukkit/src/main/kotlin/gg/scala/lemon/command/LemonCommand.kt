package gg.scala.lemon.command

import gg.scala.lemon.Lemon
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.software.SoftwareDump
import gg.scala.lemon.software.SoftwareDumpCategory
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.CommandHelp
import net.evilblock.cubed.acf.annotation.*
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 10/7/2021
 */
@CommandAlias("lemon")
@CommandPermission("op")
class LemonCommand : BaseCommand()
{
    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("dump")
    @Description("Dump general internal software details.")
    fun onDump(player: Player)
    {
        val softwareDump = SoftwareDump("System Dump")

        val system = SoftwareDumpCategory("System")
        system.addEntry(
            "Threads ${CC.GRAY}(Total)${CC.SEC}" to Thread.getAllStackTraces().values.size
        )
        system.addEntry(
            "Threads ${CC.GRAY}(Daemon)${CC.SEC}" to Thread
                .getAllStackTraces().keys
                .filter { it.isDaemon }
                .size
        )
        system.addEntry(
            "Threads ${CC.GRAY}(Interrupted)${CC.SEC}" to Thread
                .getAllStackTraces().keys
                .filter { it.isInterrupted }
                .size
        )
        system.addEntry(
            "Threads ${CC.GRAY}(Alive)${CC.SEC}" to Thread
                .getAllStackTraces().keys
                .filter { it.isAlive }
                .size
        )

        val commonPool = SoftwareDumpCategory("Common Pool")
        commonPool.addEntry(
            "Parallelism" to ForkJoinPool
                .getCommonPoolParallelism()
        )
        commonPool.addEntry(
            "Active" to (ForkJoinPool
                .getCommonPoolParallelism() > 1)
        )

        softwareDump.addCategory(system)
        softwareDump.addCategory(commonPool)

        softwareDump.formFancyMessage()
            .sendToPlayer(player)
    }

    @Subcommand("visibility-dump")
    @Description("Dump visibility debug information.")
    fun onVisibilityDump(player: Player)
    {
        Bukkit.getOnlinePlayers().forEach {
            player.sendMessage(" ")

            VisibilityHandler.getDebugInfo(player, it).forEach { message ->
                player.sendMessage(message)
            }

            VisibilityHandler.getDebugInfo(it, player).forEach { message ->
                player.sendMessage(message)
            }

            player.sendMessage(" ")
        }
    }

    @Subcommand("punishment-dump")
    @Description("Dump punishment information.")
    fun onPunishmentDump(player: Player)
    {
        player.sendMessage("${CC.YELLOW}Loading punishment information...")

        DataStoreObjectControllerCache.findNotNull<Punishment>()
            .loadAll(DataStoreStorageType.MONGO)
            .thenAccept {
                val softwareDump = SoftwareDump("Punishment Dump")

                val general = SoftwareDumpCategory("General")
                general.addEntry(
                    "Punishments ${CC.GRAY}(Total)${CC.SEC}" to it.size
                )
                general.addEntry(
                    "Punishments ${CC.GRAY}(Active)${CC.SEC}" to
                            it.filter { punishment -> punishment.value.isActive }.size
                )
                general.addEntry(
                    "Punishments ${CC.GRAY}(Removed)${CC.SEC}" to
                            it.filter { punishment -> punishment.value.isRemoved }.size
                )
                general.addEntry(
                    "Punishments ${CC.GRAY}(Permanent)${CC.SEC}" to
                            it.filter { punishment -> punishment.value.isPermanent }.size
                )

                softwareDump.addCategory(general)

                softwareDump.formFancyMessage()
                    .sendToPlayer(player)
            }
    }

    @Subcommand("grant-dump")
    @Description("Dump grant information.")
    fun onGrantDump(player: Player)
    {
        player.sendMessage("${CC.YELLOW}Loading grant information...")

        DataStoreObjectControllerCache.findNotNull<Grant>()
            .loadAll(DataStoreStorageType.MONGO)
            .thenAccept {
                val softwareDump = SoftwareDump("Grant Dump")

                val general = SoftwareDumpCategory("General")
                general.addEntry(
                    "Grants ${CC.GRAY}(Total)${CC.SEC}" to it.size
                )
                general.addEntry(
                    "Grants ${CC.GRAY}(Active)${CC.SEC}" to
                            it.filter { grant -> grant.value.isActive }.size
                )
                general.addEntry(
                    "Grants ${CC.GRAY}(Removed)${CC.SEC}" to
                            it.filter { grant -> grant.value.isRemoved }.size
                )
                general.addEntry(
                    "Grants ${CC.GRAY}(Permanent)${CC.SEC}" to
                            it.filter { grant -> grant.value.isPermanent }.size
                )

                softwareDump.addCategory(general)

                softwareDump.formFancyMessage()
                    .sendToPlayer(player)
            }
    }
}
