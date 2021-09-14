package com.solexgames.lemon.handler

import com.solexgames.lemon.Lemon
import com.solexgames.lemon.util.QuickAccess
import com.solexgames.lemon.util.QuickAccess.messageType
import com.solexgames.lemon.util.redis.RedisMessage
import com.solexgames.redis.annotation.Subscription
import com.solexgames.redis.handler.JedisHandler
import com.solexgames.redis.json.JsonAppender
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object RedisHandler: JedisHandler {

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
                it.sendMessage(channel.getFormatted(message, sender, rank, it))
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
            jsonAppender.getParam("target")
        )

        Lemon.instance.playerHandler.findPlayer(targetUuid).ifPresent {
            it.recalculatePunishments()
        }
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

    @JvmStatic
    fun buildMessage(packet: String, message: Map<String, String>): RedisMessage {
        return RedisMessage(JsonAppender(packet).also {
            message.forEach { (key, value) ->
                it.put(key, value)
            }
        }.asJson)
    }
}
