package gg.scala.lemon.handler

import gg.scala.lemon.Lemon
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.messageType
import gg.scala.lemon.util.other.FancyMessage
import gg.scala.lemon.util.redis.RedisMessage
import com.solexgames.redis.annotation.Subscription
import com.solexgames.redis.handler.JedisHandler
import com.solexgames.redis.json.JsonAppender
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class RedisHandler: JedisHandler {

    @Subscription(action = "channel-message")
    fun onChannelMessage(jsonAppender: JsonAppender) {
        val message = jsonAppender.getParam("message")
        val sender = jsonAppender.getParam("sender")

        val rank = Lemon.instance.rankHandler.findRank(
            UUID.fromString(jsonAppender.getParam("rank"))
        ) ?: Lemon.instance.rankHandler.getDefaultRank()

        val channel = Lemon.instance.chatHandler.findChannel(jsonAppender.getParam("channel")) ?: return

        Bukkit.getOnlinePlayers().forEach {
            if (channel.hasPermission(it)) {
                it.sendMessage(channel.getFormatted(message, sender, rank, it).replace("%s", jsonAppender.getParam("server")))
            }
        }
    }

    @Subscription(action = "staff-message")
    fun onStaffMessage(jsonAppender: JsonAppender) {
        val message = jsonAppender.getParam("message")
        val permission = jsonAppender.getParam("permission")
        val server = jsonAppender.getParam("server")
        val fancySender = jsonAppender.getParam("sender-fancy")

        val withServer = jsonAppender.getParam("with-server")!!.toBoolean()

        val baseMessage = "${CC.AQUA}[S] ${if (withServer) "${CC.D_AQUA}[$server] " else ""}"

        println("got message ${messageType(jsonAppender.getParam("messageType")).name}")

        when (
            messageType(jsonAppender.getParam("messageType"))
        ) {
            QuickAccess.MessageType.PLAYER_MESSAGE -> {
                sendMessage("$baseMessage$fancySender${CC.WHITE}: ${CC.AQUA}$message") {
                    return@sendMessage it.hasPermission(permission)
                }
            }
            QuickAccess.MessageType.NOTIFICATION -> {
                sendMessage("$baseMessage$message") {
                    return@sendMessage it.hasPermission(permission)
                }
            }
        }
    }

    @Subscription(action = "global-message")
    fun onGlobalMessage(jsonAppender: JsonAppender) {
        val message = jsonAppender.getParam("message")
        val permission = jsonAppender.getParam("permission")

        if (permission.isNotBlank()) {
            Bukkit.broadcast(message, permission)
        } else {
            Bukkit.broadcastMessage(message)
        }
    }

    @Subscription(action = "player-message")
    fun onPlayerMessage(jsonAppender: JsonAppender) {
        val message = jsonAppender.getParam("message")
        val targetUuid = UUID.fromString(
            jsonAppender.getParam("target")
        )

        Bukkit.getPlayer(targetUuid)?.sendMessage(message)
    }

    @Subscription(action = "global-fancy-message")
    fun onGlobalFancyMessage(jsonAppender: JsonAppender) {
        val message = Serializers.gson.fromJson(
            jsonAppender.getParam("message"),
            FancyMessage::class.java
        )
        val permission = jsonAppender.getParam("permission")

        Bukkit.getOnlinePlayers()
            .filter { permission.isBlank() || it.hasPermission(permission) }
            .forEach { message.sendToPlayer(it) }
    }

    @Subscription(action = "player-fancy-message")
    fun onPlayerFancyMessage(jsonAppender: JsonAppender) {
        val message = Serializers.gson.fromJson(
            jsonAppender.getParam("message"),
            FancyMessage::class.java
        )
        val targetUuid = UUID.fromString(
            jsonAppender.getParam("target")
        )

        val player = Bukkit.getPlayer(targetUuid)

        if (player != null) {
            message.sendToPlayer(player)
        }
    }

    @Subscription(action = "recalculate-grants")
    fun onRecalculate(jsonAppender: JsonAppender) {
        val targetUuid = UUID.fromString(
            jsonAppender.getParam("target")
        )

        Lemon.instance.playerHandler.findPlayer(targetUuid).ifPresent {
            it.recalculateGrants(
                shouldCalculateNow = true
            )
        }
    }

    @Subscription(action = "recalculate-punishments")
    fun onPunishmentHandling(jsonAppender: JsonAppender) {
        val targetUuid = UUID.fromString(
            jsonAppender.getParam("uniqueId")
        )

        Lemon.instance.playerHandler.findPlayer(targetUuid).ifPresent {
            it.recalculatePunishments()
        }
    }

    @Subscription(action = "cross-kick")
    fun onCrossKick(jsonAppender: JsonAppender) {
        val targetUuid = UUID.fromString(
            jsonAppender.getParam("uniqueId")
        )
        val reason = jsonAppender.getParam("reason")

        Bukkit.getPlayer(targetUuid)?.kickPlayer("""
            ${CC.RED}You've been kicked from${Lemon.instance.settings.id}:
            ${CC.WHITE}$reason
        """.trimIndent())
    }

    @Subscription(action = "rank-delete")
    fun onRankDelete(jsonAppender: JsonAppender) {
        val rankUuid = UUID.fromString(
            jsonAppender.getParam("uniqueId")
        )

        Lemon.instance.rankHandler.ranks.remove(rankUuid)
    }

    @Subscription(action = "rank-update")
    fun onRankUpdate(jsonAppender: JsonAppender) {
        val completableFuture = Lemon.instance.mongoHandler.rankLayer
            .fetchEntryByKey(jsonAppender.getParam("uniqueId"))

        completableFuture.thenAccept {
            Lemon.instance.rankHandler.ranks[it.uuid] = it
        }
    }

    private fun sendMessage(message: String, permission: (Player) -> Boolean) {
        Bukkit.getOnlinePlayers().forEach {
            if (permission.invoke(it)) {
                it.sendMessage(message)
            }
        }
    }

    companion object {
        @JvmStatic
        fun buildMessage(packet: String, message: Map<String, String>): RedisMessage {
            return RedisMessage(JsonAppender(packet).also {
                message.forEach { (key, value) ->
                    it.put(key, value)
                }
            }.asJson)
        }
    }
}
