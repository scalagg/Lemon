package com.solexgames.lemon.util

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.handler.RedisHandler
import com.solexgames.lemon.util.other.Cooldown
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author puugz, GrowlyX
 */
object QuickAccess {

    fun coloredNameOrConsole(sender: CommandSender): String {
        val lemonPlayer = sender.name?.let { Lemon.instance.playerHandler.findPlayer(it).orElse(null) }

        lemonPlayer?.let {
            return it.getColoredName()
        } ?: return "${CC.D_RED}Console"
    }

    fun coloredName(name: String?): String? {
        val lemonPlayer = name?.let { Lemon.instance.playerHandler.findPlayer(it).orElse(null) }

        lemonPlayer?.let {
            return it.getColoredName()
        } ?: return name
    }

    fun coloredName(uuid: UUID): String? {
        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(uuid).orElse(null)

        lemonPlayer?.let {
            return it.getColoredName()
        }  ?: return null
    }

    fun coloredName(player: Player): String? {
        return coloredName(player.uniqueId)
    }

    fun reloadPlayer(uuid: UUID) {
        Bukkit.getPlayer(uuid)?.let {
            Lemon.instance.playerHandler.findPlayer(it).ifPresent { lemonPlayer ->
                NametagHandler.reloadPlayer(it)
                VisibilityHandler.update(it)

                lemonPlayer.recalculateGrants(
                    shouldCalculateNow = true
                )
            }
        }
    }

    fun remaining(cooldown: Cooldown): String {
        return String.format("%.0f", (cooldown.getRemaining() / 1000).toFloat())
    }

    fun replaceEmpty(string: String): String {
        return string.ifBlank {
            "${CC.RED}None"
        }
    }

    fun senderUuid(sender: CommandSender): UUID? {
        return if (sender is ConsoleCommandSender) null else (sender as Player).uniqueId
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun sendStaffMessage(
        sender: CommandSender,
        message: String,
        addServer: Boolean,
        messageType: MessageType
    ): CompletableFuture<Void> {
        return RedisHandler.buildMessage(
            "staff-message",
            buildMap {
                put("sender-fancy", coloredNameOrConsole(sender))
                put("message", message)
                put("permission", "lemon.staff")
                put("messageType", messageType.name)

                put("server", Lemon.instance.settings.id)
                put("with-server", addServer.toString())
            }
        ).publishAsync()
    }

    fun messageType(name: String): MessageType {
        return MessageType.valueOf(name)
    }

    enum class MessageType {
        PLAYER_MESSAGE,
        NOTIFICATION
    }
}
