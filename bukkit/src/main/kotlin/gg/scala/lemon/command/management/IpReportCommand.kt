package gg.scala.lemon.command.management

import net.evilblock.cubed.acf.BaseCommand
import net.evilblock.cubed.acf.annotation.CommandAlias
import net.evilblock.cubed.acf.annotation.CommandPermission
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.*

/**
 * @author GrowlyX
 * @since 10/3/2021
 */
class IpReportCommand : BaseCommand() {

    @CommandAlias("ipreport")
    @CommandPermission("lemon.command.ipreport")
    fun onIpReport(sender: CommandSender) {
        val mutableMap = mutableMapOf<String, MutableList<UUID>>()

        Bukkit.getOnlinePlayers().forEach {
            val address = it.player.address.address.hostAddress
            val mapEntry = mutableMap[address]

            if (mapEntry == null) {
                mutableMap[address] = mutableListOf(it.uniqueId)
            } else {
                mapEntry.add(it.uniqueId)
            }
        }

        sender.sendMessage("${CC.B_PRI}IP Report:")
        sender.sendMessage("${CC.SEC}${mutableMap.size}${CC.GRAY} ip pair was found.")
        sender.sendMessage("")

        mutableMap.forEach {
            it.value.forEach { uuid ->
                val player = Bukkit.getPlayer(uuid)

                if (player != null) {
                    sender.sendMessage(" ${player.displayName}")
                }
            }

            sender.sendMessage("")
        }
    }
}
