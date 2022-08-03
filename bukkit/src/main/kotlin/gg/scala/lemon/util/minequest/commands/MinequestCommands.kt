package gg.scala.lemon.util.minequest.commands

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.command.ScalaCommand
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.lemon.util.minequest.MinequestChatImages
import gg.scala.lemon.util.minequest.MinequestIcon
import gg.scala.lemon.util.minequest.platinum.menu.PlatinumColorChangeMenu
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

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
        val purchase =
            MinequestChatImages.purchaseImage()

        Bukkit.broadcastMessage("")

        for ((index, str) in purchase.withIndex())
        {
            when (index + 1)
            {
                1 -> Bukkit.broadcastMessage(" 䰘" + str + "     " + ChatColor.BOLD + target.username() + ChatColor.AQUA + " supported the server by")
                2 -> Bukkit.broadcastMessage(" 䰘" + str + "     " + ChatColor.AQUA + "purchasing an item from the store!")
                3 -> Bukkit.broadcastMessage(" 䰘$str")
                4 -> Bukkit.broadcastMessage(" 䰘$str")
                5 -> Bukkit.broadcastMessage(" 䰘" + str + "     " + ChatColor.GRAY + "Say " + ChatColor.WHITE + ChatColor.BOLD.toString() + "'GG'" + ChatColor.GRAY + " in chat for " + ChatColor.GOLD + "10 " + ChatColor.WHITE + MinequestIcon.COIN.character)
                6 -> Bukkit.broadcastMessage(" 䰘" + str + "     " + ChatColor.AQUA + "store.minequest.gg")
            }
        }

        Bukkit.broadcastMessage("")
        // TODO: handle gg coin logic
    }
}
