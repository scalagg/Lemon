package gg.scala.lemon.util.minequest.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.lemon.util.minequest.MinequestChatImages
import gg.scala.lemon.util.minequest.MinequestIcon
import gg.scala.lemon.util.minequest.collection.MinequestCoinCollectionLogic
import gg.scala.lemon.util.minequest.platinum.menu.PlatinumColorChangeMenu
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 7/25/2022
 */
object MinequestCommands : ScalaCommand()
{
    @CommandAlias("platinum-color|platinum")
    @CommandPermission("rank.platinum")
    fun onPlatinum(player: Player)
    {
        PlatinumColorChangeMenu().openMenu(player)
    }

    @CommandAlias("purchase")
    @CommandPermission("op")
    fun onPurchase(
        sender: CommandSender,
        target: UUID
    )
    {
        Bukkit.broadcastMessage("")

        Bukkit.getOnlinePlayers().forEach {
            val thankYou = MinequestChatImages.thankYouImage(it)

            thankYou.forEach { line ->
                it.sendMessage(line)
            }
        }

        Bukkit.broadcastMessage("")
        Bukkit.broadcastMessage(CC.B_AQUA + target.username() + ChatColor.AQUA + " supported the server by")
        Bukkit.broadcastMessage(ChatColor.AQUA.toString() + "purchasing an item from the store!")
        Bukkit.broadcastMessage("")
        Bukkit.broadcastMessage("")
        Bukkit.broadcastMessage(ChatColor.GRAY.toString() + "Say " + ChatColor.WHITE + ChatColor.BOLD.toString() + "GG" + ChatColor.GRAY + " in chat for " + ChatColor.GOLD + "10 " + ChatColor.WHITE + MinequestIcon.COIN.character + CC.GRAY + "!")
        Bukkit.broadcastMessage(ChatColor.DARK_AQUA.toString() + "store.minequest.gg")
        Bukkit.broadcastMessage("")

        val coinCollection = mutableListOf<UUID>()

        Events
            .subscribe(
                AsyncPlayerChatEvent::class.java,
                EventPriority.HIGHEST
            )
            .expireAfter(10L, TimeUnit.SECONDS)
            .filter {
                !coinCollection.contains(it.player.uniqueId) &&
                        it.message.lowercase().startsWith("gg")
            }
            .handler {
                coinCollection.add(it.player.uniqueId)

                kotlin
                    .runCatching {
                        MinequestCoinCollectionLogic.onCollection(it.player)
                    }
                    .onFailure { throwable ->
                        throwable.printStackTrace()
                    }
            }
    }
}
