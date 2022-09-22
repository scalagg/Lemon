package gg.scala.lemon.handler

import gg.scala.aware.annotation.Subscribe
import gg.scala.aware.message.AwareMessage
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.lemon.Lemon
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.software.task.ResourceUpdateRunnable
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.broadcast
import gg.scala.lemon.util.QuickAccess.username
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.util.bukkit.Tasks.sync
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

object RedisHandler
{
    @Subscribe("channel-message")
    fun onChannelMessage(
        message: AwareMessage
    )
    {
        val content = message
            .retrieve<String>("message")

        val sender = message
            .retrieve<UUID>("sender")

        val rank = RankHandler.findRank(
            message.retrieve<UUID>("rank")
        ) ?: RankHandler.getDefaultRank()

        val server = message
            .retrieve<String>("server")

        val channel = ChatChannelService
            .find(
                message.retrieve("channel")
            )
            ?: return

        for (other in Bukkit.getOnlinePlayers())
        {
            if (!channel.permissionLambda.invoke(other))
                continue

            channel.sendToPlayer(
                other, channel.composite()
                    .format(
                        sender, other, content,
                        server, rank
                    )
            )
        }
    }

    @Subscribe("global-message")
    fun onGlobalMessage(message: AwareMessage)
    {
        val newMessage = message
            .retrieve<String>("message")

        val permission = message
            .retrieveNullable<String>("permission")

        if (!permission.isNullOrBlank())
        {
            broadcast(newMessage, permission)
            return
        }

        Bukkit.broadcastMessage(newMessage)
    }

    @Subscribe("mass-whitelist")
    fun onMassWhitelist(message: AwareMessage)
    {
        val group = message
            .retrieve<String>("group")

        val setting = message
            .retrieve<Boolean>("setting")

        val groups = ServerSync
            .getLocalGameServer().groups

        if (group.lowercase() in groups)
        {
            Bukkit.setWhitelist(setting)

            broadcast(
                "${CC.AQUA}[S] ${CC.D_AQUA}Whitelist has been ${
                    if (setting)
                    {
                        "${CC.GREEN}enabled"
                    } else
                    {
                        "${CC.RED}disabled"
                    }
                }${CC.D_AQUA} externally.",
                "lemon.security.notifications"
            )
        }
    }

    @Subscribe("player-message")
    fun onPlayerMessage(message: AwareMessage)
    {
        val newMessage = message
            .retrieve<String>("message")

        val targetUuid = message
            .retrieve<UUID>("target")

        Bukkit.getPlayer(targetUuid)
            ?.sendMessage(newMessage)
    }

    @Subscribe("global-fancy-message")
    fun onGlobalFancyMessage(message: AwareMessage)
    {
        val newMessage = message
            .retrieve<FancyMessage>("message")

        val permission = message
            .retrieveNullable<String>("permission")

        val metaPermission = message
            .retrieveNullable<String>("meta-permission")

        Bukkit.getOnlinePlayers()
            .filter { permission == null || it.hasPermission(permission) }
            .forEach {
                val specificMessage = FancyMessage()
                    .apply {
                        this.components.addAll(
                            newMessage.components
                        )
                    }

                if (metaPermission != null && !it.hasPermission(metaPermission))
                {
                    for (component in specificMessage.components)
                    {
                        component.clickEvent = null
                        component.hoverMessage = null
                    }
                }

                specificMessage.sendToPlayer(it)
            }
    }

    @Subscribe("player-fancy-message")
    fun onPlayerFancyMessage(message: AwareMessage)
    {
        val newMessage = message
            .retrieve<FancyMessage>("message")

        val targetUuid = message
            .retrieve<UUID>("target")

        val player = Bukkit.getPlayer(targetUuid)

        if (player != null)
        {
            newMessage.sendToPlayer(player)
        }
    }

    @Subscribe("recalculate-grants")
    fun onRecalculate(message: AwareMessage)
    {
        val targetUuid = message
            .retrieve<UUID>("target")

        PlayerHandler.findPlayer(targetUuid)
            .ifPresent {
                it.recalculateGrants(
                    shouldCalculateNow = true
                )
            }
    }

    @Subscribe("recalculate-punishments")
    fun onPunishmentHandling(message: AwareMessage)
    {
        val targetUuid = message
            .retrieve<UUID>("uniqueId")

        PlayerHandler.findPlayer(targetUuid).ifPresent {
            it.recalculatePunishments()
        }
    }

    @Subscribe("reload-player")
    fun onReloadPlayer(message: AwareMessage)
    {
        val targetUuid = message
            .retrieve<UUID>("uniqueId")

        PlayerHandler.findPlayer(targetUuid).ifPresent {
            QuickAccess.reloadPlayer(targetUuid)
        }
    }

    @Subscribe("cross-kick")
    fun onCrossKick(message: AwareMessage)
    {
        val targetUuid = message
            .retrieve<UUID>("uniqueId")

        val reason = message
            .retrieve<String>("reason")

        sync {
            Bukkit.getPlayer(targetUuid)
                ?.kickPlayer(
                    Lemon.instance.languageConfig.kickMessage
                        .format(
                            Lemon.instance.settings.id, reason
                        )
                )
        }
    }

    @Subscribe("rank-delete")
    fun onRankDelete(message: AwareMessage)
    {
        val rankUuid = message
            .retrieve<UUID>("uniqueId")

        RankHandler.ranks.remove(rankUuid)

        ResourceUpdateRunnable.run()
    }

    @Subscribe("rank-update")
    fun onRankUpdate(message: AwareMessage)
    {
        val completableFuture = DataStoreObjectControllerCache
            .findNotNull<Rank>()
            .load(
                message.retrieve("uniqueId"),
                DataStoreStorageType.MONGO
            )

        completableFuture.thenAccept {
            it?.let { rank ->
                RankHandler.ranks[rank.uuid] = rank
            }
        }
    }

    private fun sendMessage(message: String, permission: (Player) -> Boolean)
    {
        Bukkit.getOnlinePlayers().forEach {
            if (permission.invoke(it))
            {
                it.sendMessage(message)
            }
        }
    }

    fun buildMessage(
        packet: String,
        message: Map<String, String>
    ): AwareMessage
    {
        return AwareMessage.of(
            packet, Lemon.instance.aware
        ).apply {
            content.putAll(message)
        }
    }

    fun buildMessage(
        packet: String,
        vararg pairs: Pair<String, Any?>
    ): AwareMessage
    {
        return AwareMessage.of(
            packet, Lemon.instance.aware, *pairs
        )
    }
}
