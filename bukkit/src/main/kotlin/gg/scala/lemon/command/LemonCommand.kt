package gg.scala.lemon.command

import gg.scala.commons.acf.CommandHelp
import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.acf.annotation.Description
import gg.scala.commons.acf.annotation.HelpCommand
import gg.scala.commons.acf.annotation.Subcommand
import gg.scala.commons.annotations.commands.AssignPermission
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.software.SoftwareDump
import gg.scala.lemon.software.SoftwareDumpCategory
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import kong.unirest.Unirest
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.visibility.VisibilityHandler
import net.md_5.bungee.api.chat.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX
 * @since 10/7/2021
 */
@AutoRegister
@CommandAlias("scl")
@CommandPermission("lemon.command.lemon")
object LemonCommand : ScalaCommand()
{
    @Inject
    lateinit var plugin: Lemon

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("run-migrations")
    @Description("Run account migrations.")
    fun onRunMigrations(
        console: ConsoleCommandSender
    ): CompletableFuture<Void>
    {
        return DataStoreObjectControllerCache
            .findNotNull<LemonPlayer>()
            .loadAll(DataStoreStorageType.MONGO)
            .thenAcceptAsync {
                for (lemonPlayer in it.values)
                {
                    lemonPlayer.save().join()
                }

                console.sendMessage("${CC.GREEN}Completed migrations.")
            }
    }

    @AssignPermission
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

    @AssignPermission
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

    @AssignPermission
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

    enum class ExportSpec
    {
        GRANT, PUNISHMENT, RANK
    }

    @AssignPermission
    @Subcommand("export")
    @Description("Export information.")
    fun onExport(player: Player, spec: ExportSpec): CompletableFuture<Void>
    {
        val controller = when (spec)
        {
            ExportSpec.GRANT -> DataStoreObjectControllerCache.findNotNull<Grant>()
            ExportSpec.RANK -> DataStoreObjectControllerCache.findNotNull<Rank>()
            ExportSpec.PUNISHMENT -> DataStoreObjectControllerCache.findNotNull<Punishment>()
        }

        player.sendMessage("${CC.GREEN}Uploading content to pastes.dev...")

        return controller
            .loadAll(DataStoreStorageType.MONGO)
            .thenAcceptAsync {
                val mappings = Serializers
                    .gson.toJson(it.values)

                val response = Unirest
                    .post("https://api.pastes.dev/post")
                    .body(mappings)
                    .contentType("text/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0")
                    .asJson()

                val key = response
                    .headers["Location"]
                    .firstOrNull()

                val fancy = FancyMessage()
                    .withMessage("${CC.GREEN}The content is available at: ")
                    .withMessage("${CC.WHITE}${"https://pastes.dev/$key/"}")
                    .andCommandOf(
                        ClickEvent.Action.OPEN_URL,
                        "https://pastes.dev/$key/"
                    )
                    .andHoverOf(
                        "${CC.YELLOW}Click to open the page!"
                    )

                fancy.sendToPlayer(player)
            }
    }

    @AssignPermission
    @CommandAlias("services")
    @Description("View all enabled services.")
    fun onDefault(sender: CommandSender)
    {
        val services = plugin.flavor().services
        sender.sendMessage("${CC.SEC}Loaded services ${CC.GRAY}(${services.size})${CC.SEC}: ${CC.PRI}${
            services.values
                .map { it.javaClass.getAnnotation(Service::class.java) to it }
                .joinToString(
                    separator = "${CC.SEC}, ${CC.PRI}"
                ) {
                    it.first.name.ifBlank {
                        it.second.javaClass.simpleName
                    }
                }
        }")
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
