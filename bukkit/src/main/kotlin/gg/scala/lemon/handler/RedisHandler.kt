package gg.scala.lemon.handler

import gg.scala.banana.annotate.Subscribe
import gg.scala.banana.message.Message
import gg.scala.banana.subscribe.marker.BananaHandler
import gg.scala.lemon.Lemon
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks.sync
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object RedisHandler : BananaHandler {

    @Subscribe("channel-message")
    fun onChannelMessage(message: Message) {
        val newMessage = message["message"]!!
        val sender = message["sender"]!!

        val rank = RankHandler.findRank(
            UUID.fromString(message["rank"])
        ) ?: RankHandler.getDefaultRank()

        val channel = ChatHandler.findChannel(message["channel"]!!) ?: return

        Bukkit.getOnlinePlayers().forEach {
            if (channel.hasPermission(it)) {
                it.sendMessage(
                    channel.getFormatted(newMessage, sender, rank, it).replace("%s", message["server"]!!)
                )
            }
        }
    }

    @Subscribe(
        value = "staff-message",
        priority = 10
    )
    fun onStaffMessage(message: Message) {
        val newMessage = message["message"]
        val permission = message["permission"]

        val server = message["server"]
        val potentialFlag = message["flag"]
        val withServer = message["with-server"]!!.toBoolean()

        val baseMessage = "${CC.AQUA}[S] ${if (withServer) "${CC.D_AQUA}[$server] " else ""}"

        sendMessage("$baseMessage$newMessage") {
            if (permission == null) return@sendMessage true

            val lemonPlayer = PlayerHandler.findPlayer(it).orElse(null)

            return@sendMessage if (lemonPlayer != null) {
                val base = lemonPlayer.hasPermission(permission) && !lemonPlayer.getSetting("staff-messages-disabled")

                if (potentialFlag != null) {
                    lemonPlayer.hasPermission(permission) && !lemonPlayer.getSetting(potentialFlag)
                } else base
            } else false
        }
    }

    @Subscribe("global-message")
    fun onGlobalMessage(message: Message) {
        val newMessage = message["message"]
        val permission = message["permission"]

        if (permission!!.isNotBlank()) {
            Bukkit.broadcast(newMessage, permission)
        } else {
            Bukkit.broadcastMessage(newMessage)
        }
    }

    @Subscribe("mass-whitelist")
    fun onMassWhitelist(message: Message) {
        val group = message["group"]!!
        val issuer = message["issuer"]!!
        val setting = message["setting"]!!

        if (Lemon.instance.getLocalServerInstance().serverGroup.equals(group, true)) {
            Bukkit.setWhitelist(setting.toBoolean())

            Bukkit.broadcast(
                "${CC.AQUA}[S] [External] ${CC.D_AQUA}Whitelist has been set to ${CC.AQUA}$setting${CC.D_AQUA}.",
                "lemon.security.notifications"
            )

            println("[Security] $issuer has set whitelist to $setting.")
        }
    }

    @Subscribe("player-message")
    fun onPlayerMessage(message: Message) {
        val newMessage = message["message"]
        val targetUuid = UUID.fromString(
            message["target"]
        )

        Bukkit.getPlayer(targetUuid)?.sendMessage(newMessage)
    }

    @Subscribe("global-fancy-message")
    fun onGlobalFancyMessage(message: Message) {
        val newMessage = Serializers.gson.fromJson(
            message["message"],
            FancyMessage::class.java
        )
        val permission = message["permission"]

        Bukkit.getOnlinePlayers()
            .filter { permission!!.isBlank() || it.hasPermission(permission) }
            .forEach { newMessage.sendToPlayer(it) }
    }

    @Subscribe("player-fancy-message")
    fun onPlayerFancyMessage(message: Message) {
        val newMessage = Serializers.gson.fromJson(
            message["message"],
            FancyMessage::class.java
        )
        val targetUuid = UUID.fromString(
            message["target"]
        )

        val player = Bukkit.getPlayer(targetUuid)

        if (player != null) {
            newMessage.sendToPlayer(player)
        }
    }

    @Subscribe("recalculate-grants")
    fun onRecalculate(message: Message) {
        val targetUuid = UUID.fromString(
            message["target"]
        )

        PlayerHandler.findPlayer(targetUuid).ifPresent {
            it.recalculateGrants(
                shouldCalculateNow = true
            )
        }
    }

    @Subscribe("recalculate-punishments")
    fun onPunishmentHandling(message: Message) {
        val targetUuid = UUID.fromString(
            message["uniqueId"]
        )

        PlayerHandler.findPlayer(targetUuid).ifPresent {
            it.recalculatePunishments()
        }
    }

    @Subscribe("reload-player")
    fun onReloadPlayer(message: Message) {
        val targetUuid = UUID.fromString(
            message["uniqueId"]
        )

        PlayerHandler.findPlayer(targetUuid).ifPresent {
            QuickAccess.reloadPlayer(targetUuid, recalculateGrants = true)
        }
    }

    @Subscribe("cross-kick")
    fun onCrossKick(message: Message) {
        val targetUuid = UUID.fromString(
            message["uniqueId"]
        )
        val reason = message["reason"]

        sync {
            Bukkit.getPlayer(targetUuid)?.kickPlayer(
                """
                    ${CC.RED}You've been kicked from${Lemon.instance.settings.id}:
                    ${CC.WHITE}$reason
                """.trimIndent()
            )
        }
    }

    @Subscribe("rank-delete")
    fun onRankDelete(message: Message) {
        val rankUuid = UUID.fromString(
            message["uniqueId"]
        )

        RankHandler.ranks.remove(rankUuid)
    }

    @Subscribe("rank-update")
    fun onRankUpdate(message: Message) {
        val completableFuture = DataStoreHandler.rankLayer
            .fetchEntryByKey(message["uniqueId"])

        completableFuture.thenAccept {
            RankHandler.ranks[it.uuid] = it
        }
    }

    private fun sendMessage(message: String, permission: (Player) -> Boolean) {
        Bukkit.getOnlinePlayers().forEach {
            if (permission.invoke(it)) {
                it.sendMessage(message)
            }
        }
    }

    fun buildMessage(packet: String, message: Map<String, String>): Message {
        return Message(packet).also {
            message.forEach { (key, value) ->
                it[key] = value
            }
        }
    }
}
