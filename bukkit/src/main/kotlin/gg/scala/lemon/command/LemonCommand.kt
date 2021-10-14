package gg.scala.lemon.command

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.DataStoreHandler
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
object LemonCommand : BaseCommand()
{

    @Default
    @HelpCommand
    fun onHelp(help: CommandHelp)
    {
        help.showHelp()
    }

    @Subcommand("dump")
    @Description("Dump general internal software details.")
    fun onDump(sender: CommandSender)
    {
        sender.sendMessage("=== Lemon Dump ===")
        sender.sendMessage("Jedis:")
        sender.sendMessage(" Subscriptions: ${Lemon.instance.banana.subscriptions.size}")
        sender.sendMessage(" Handling Async: ${Lemon.instance.banana.options.async}")
        sender.sendMessage(" Ignoring Non-Existent Handlers: ${Lemon.instance.banana.options.ignoreNonExistentHandlers}")
        sender.sendMessage("")
        sender.sendMessage("System:")
        sender.sendMessage(" Threads: ${Thread.getAllStackTraces().values.size}")
        sender.sendMessage(" Common Pool Parallelism: ${ForkJoinPool.getCommonPoolParallelism()}")
        sender.sendMessage(" Using Common Pool: ${ForkJoinPool.getCommonPoolParallelism() > 1}")
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
    fun onPunishmentDump(sender: CommandSender)
    {
        sender.sendMessage("${CC.RED}Loading punishment information...")

        DataStoreHandler.punishmentLayer.fetchAllEntries().thenAccept {
            sender.sendMessage("${it.size} punishments exist.")
        }
    }

    @Subcommand("grant-dump")
    @Description("Dump grant information.")
    fun onGrantDump(sender: CommandSender)
    {
        sender.sendMessage("${CC.RED}Loading grant information...")

        DataStoreHandler.grantLayer.fetchAllEntries().thenAccept {
            sender.sendMessage("${it.size} grants exist.")
        }
    }
}
