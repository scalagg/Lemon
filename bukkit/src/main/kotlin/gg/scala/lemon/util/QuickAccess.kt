package gg.scala.lemon.util

import gg.scala.aware.thread.AwareThreadContext
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.*
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.rank.Rank
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import me.lucko.helper.network.Server
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.serializers.Serializers.gson
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX, puugz
 */
object QuickAccess
{
    @JvmStatic
    fun UUID.username(): String =
        CubedCacheUtil.fetchName(this)!!

    @JvmStatic
    fun String.uniqueId(): UUID =
        CubedCacheUtil.fetchUuid(this)!!

    @JvmStatic
    fun broadcast(message: String, permission: String = "")
    {
        Bukkit.getOnlinePlayers()
            .filter { permission != "" && it.hasPermission(permission) }
            .forEach {
                it.sendMessage(message)
            }

        Bukkit.getConsoleSender().sendMessage(message)
    }

    @JvmStatic
    fun nameOrConsole(sender: CommandSender): String
    {
        if (sender is ConsoleCommandSender)
        {
            return LemonConstants.CONSOLE
        }

        val lemonPlayer = PlayerHandler.findPlayer(sender as Player).orElse(null)

        lemonPlayer?.let {
            return it.getColoredName()
        } ?: return LemonConstants.CONSOLE
    }

    @JvmStatic
    fun nameOrConsole(uuid: UUID?): String
    {
        uuid ?: return LemonConstants.CONSOLE

        return CubedCacheUtil.fetchName(uuid)!!
    }

    @JvmStatic
    fun coloredName(name: String?): String?
    {
        val lemonPlayer = name?.let { PlayerHandler.findOnlinePlayer(it) }

        lemonPlayer?.let {
            return it.getOriginalColoredName()
        } ?: return name
    }

    @JvmStatic
    fun coloredNameOrNull(name: String): String?
    {
        return PlayerHandler.findPlayer(name)
            .orElse(null)?.getOriginalColoredName()
    }

    @JvmStatic
    fun coloredName(uuid: UUID): String?
    {
        val lemonPlayer = PlayerHandler.findPlayer(uuid).orElse(null)

        lemonPlayer?.let {
            return it.getColoredName()
        } ?: return null
    }

    @JvmStatic
    fun fetchColoredName(uuid: UUID?): String
    {
        uuid ?: return LemonConstants.CONSOLE

        val grants = GrantHandler.fetchGrantsFor(uuid).get()

        val playerName = CubedCacheUtil.fetchName(uuid)
        val prominent = GrantRecalculationUtil.getProminentGrant(grants)
            ?: return RankHandler.getDefaultRank().color + playerName

        return prominent.getRank().color + playerName
    }

    @JvmStatic
    fun computeColoredName(uuid: UUID, name: String): CompletableFuture<String>
    {
        return GrantHandler.fetchGrantsFor(uuid).thenApplyAsync {
            val prominent = GrantRecalculationUtil.getProminentGrant(it)
                ?: return@thenApplyAsync RankHandler.getDefaultRank().color + name

            return@thenApplyAsync prominent.getRank().color + name
        }
    }

    @JvmStatic
    fun fetchRankWeight(uuid: UUID?): CompletableFuture<Int>
    {
        return GrantHandler.fetchGrantsFor(uuid).thenApplyAsync {
            it ?: return@thenApplyAsync 0

            val prominent = GrantRecalculationUtil.getProminentGrant(it)
                ?: return@thenApplyAsync 0

            return@thenApplyAsync prominent.getRank().weight
        }
    }

    @JvmStatic
    fun fetchIpAddress(uuid: UUID): CompletableFuture<String?>
    {
        return DataStoreObjectControllerCache.findNotNull<LemonPlayer>()
            .load(uuid, DataStoreStorageType.MONGO)
            .thenApply {
                it ?: return@thenApply null

                return@thenApply it.previousIpAddress
            }
    }

    @JvmStatic
    fun coloredName(player: Player): String?
    {
        return coloredName(player.uniqueId)
    }

    @JvmStatic
    fun reloadPlayer(uuid: UUID, recalculateGrants: Boolean = true)
    {
        Bukkit.getPlayer(uuid)?.let {
            PlayerHandler.findPlayer(it).ifPresent { lemonPlayer ->
                it.displayName = lemonPlayer.getColoredName()
                it.playerListName = lemonPlayer.getColoredName(customColor = false)

                NametagHandler.reloadPlayer(it)
                VisibilityHandler.update(it)

                if (recalculateGrants)
                {
                    lemonPlayer.recalculateGrants(
                        shouldCalculateNow = true
                    )
                }
            }
        }
    }

    @JvmStatic
    fun uuidOf(sender: CommandSender): UUID?
    {
        return if (sender is Player)
        {
            sender.uniqueId
        } else null
    }

    @JvmStatic
    fun replaceEmpty(string: String): String
    {
        return ChatColor.stripColor(string).ifBlank {
            "${CC.RED}None"
        }
    }

    @JvmStatic
    fun senderUuid(sender: CommandSender): UUID?
    {
        return if (sender is ConsoleCommandSender) null else (sender as Player).uniqueId
    }

    @JvmStatic
    fun server(uniqueId: UUID): CompletableFuture<Server?>
    {
        return CompletableFuture.supplyAsync {
            val network = Lemon.instance.network

            return@supplyAsync network.servers.values
                .firstOrNull {
                    it.onlinePlayers
                        .containsKey(uniqueId)
                }
        }
    }


    @JvmStatic
    fun online(uniqueId: UUID): CompletableFuture<Boolean>
    {
        return CompletableFuture.supplyAsync {
            val network = Lemon.instance.network

            return@supplyAsync network.servers
                .any {
                    it.value.onlinePlayers
                        .containsKey(uniqueId)
                }
        }
    }

    @JvmStatic
    fun sendStaffMessage(
        sender: CommandSender?,
        message: String,
        addServer: Boolean,
        messageType: MessageType
    ): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "staff-message",
                hashMapOf(
                    "sender-fancy" to (sender?.let { nameOrConsole(sender) } ?: ""),
                    "message" to message,
                    "permission" to "lemon.staff",
                    "message-type" to messageType.name,
                    "server" to Lemon.instance.settings.id,
                    "with-server" to addServer.toString(),
                )
            ).publish(AwareThreadContext.SYNC)
        }
    }

    @JvmStatic
    fun sendStaffMessageWithFlag(
        sender: CommandSender?,
        message: String,
        addServer: Boolean,
        messageType: MessageType,
        flag: String
    ): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "staff-message",
                hashMapOf(
                    "sender-fancy" to (sender?.let { nameOrConsole(sender) } ?: ""),
                    "message" to message,
                    "permission" to "lemon.staff",
                    "flag" to flag,
                    "message-type" to messageType.name,
                    "server" to Lemon.instance.settings.id,
                    "with-server" to addServer.toString(),
                )
            ).publish(AwareThreadContext.SYNC)
        }
    }

    @JvmStatic
    fun isSilent(reason: String?) = reason?.endsWith("-s", true) == true || reason?.startsWith("-s", true) ?: false

    @JvmStatic
    fun parseReason(
        reason: String?,
        fallback: String = "Unfair Advantage"
    ): String
    {
        var preParsedReason = reason ?: fallback
        preParsedReason = preParsedReason.removePrefix("-s ")
        preParsedReason = preParsedReason.removeSuffix(" -s")

        preParsedReason = preParsedReason.removePrefix("-S ")
        preParsedReason = preParsedReason.removeSuffix(" -S")

        preParsedReason = preParsedReason.removePrefix("-S")
        preParsedReason = preParsedReason.removeSuffix("-S")

        preParsedReason = preParsedReason.removePrefix("-s")
        preParsedReason = preParsedReason.removeSuffix("-s")

        return preParsedReason.ifBlank { fallback }
    }

    @JvmStatic
    fun attemptRemoval(
        punishment: Punishment,
        reason: String = "Expired",
        remover: UUID? = null
    )
    {
        punishment.isRemoved = true
        punishment.removedAt = System.currentTimeMillis()
        punishment.removedOn = Lemon.instance.settings.id
        punishment.removedBy = remover
        punishment.removedReason = reason

        punishment.save().thenAccept {
            RedisHandler.buildMessage(
                "recalculate-punishments",
                hashMapOf(
                    "uniqueId" to punishment.target.toString()
                )
            ).publish(AwareThreadContext.SYNC)
        }
    }

    @JvmStatic
    fun attemptExpiration(punishment: Punishment, reason: String = "Expired", remover: UUID? = null): Boolean
    {
        return if (!punishment.isRemoved && punishment.hasExpired && !punishment.category.instant)
        {
            punishment.isRemoved = true
            punishment.removedAt = System.currentTimeMillis()
            punishment.removedOn = Lemon.instance.settings.id
            punishment.removedBy = remover
            punishment.removedReason = reason

            punishment.save().thenRun {
                RedisHandler.buildMessage(
                    "recalculate-punishments",
                    hashMapOf(
                        "uniqueId" to punishment.target.toString()
                    )
                ).publish(AwareThreadContext.SYNC)
            }

            false
        } else true
    }

    @JvmStatic
    fun sendGlobalBroadcast(message: String, permission: String? = null): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "global-message",
                "message" to message,
                "permission" to (permission ?: "")
            ).publish(AwareThreadContext.SYNC)
        }
    }

    @JvmStatic
    fun sendGlobalFancyBroadcast(fancyMessage: FancyMessage, permission: String?): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "global-fancy-message",
                "message" to gson.toJson(fancyMessage),
                "permission" to (permission ?: "")
            ).publish(AwareThreadContext.SYNC)
        }.exceptionally {
            it.printStackTrace()
            return@exceptionally null
        }
    }

    @JvmStatic
    fun sendGlobalPlayerMessage(message: String, uuid: UUID): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "player-message",
                hashMapOf(
                    "message" to message,
                    "target" to uuid.toString()
                )
            ).publish(AwareThreadContext.SYNC)
        }
    }

    @JvmStatic
    fun sendGlobalPlayerFancyMessage(fancyMessage: FancyMessage, uuid: UUID): CompletableFuture<Void>
    {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "player-fancy-message",
                hashMapOf(
                    "message" to gson.toJson(fancyMessage),
                    "target" to uuid.toString()
                )
            ).publish(AwareThreadContext.SYNC)
        }
    }

    @JvmStatic
    fun messageType(name: String): MessageType
    {
        return MessageType.valueOf(name)
    }

    @JvmStatic
    fun weightOf(issuer: CommandSender): Int
    {
        if (issuer is ConsoleCommandSender)
        {
            return Int.MAX_VALUE
        }

        val lemonPlayer = PlayerHandler.findPlayer(issuer as Player).orElse(null)

        lemonPlayer?.let {
            return it.activeGrant!!.getRank().weight
        }

        return 0
    }

    @JvmStatic
    fun shouldBlock(player: Player): Boolean
    {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (!lemonPlayer.hasPermission("lemon.2fa.forced")) return false
        if (lemonPlayer.isAuthExempt()) return false
        if (lemonPlayer.hasAuthenticatedThisSession()) return false

        return true
    }

    @JvmStatic
    fun toNiceString(string: String): String
    {
        var output = string.lowercase(Locale.getDefault()).replace("_", " ").trim()

        for (s in output.split(" "))
        {
            val char = s[0]

            if (!char.isUpperCase())
            {
                val replace = s.replaceFirst(char, char.uppercaseChar())
                output = output.replace(s, replace)
            }
        }

        return output
    }

    fun realRank(player: Player?): Rank
    {
        player ?: return RankHandler.getDefaultRank()

        val lemonPlayer = PlayerHandler.findPlayer(player.uniqueId).orElse(null)

        return if (lemonPlayer != null && (player.name == lemonPlayer.name || !player.hasMetadata("disguised")))
        {
            lemonPlayer.activeGrant?.getRank() ?: RankHandler.getDefaultRank()
        } else
        {
            RankHandler.getDefaultRank()
        }
    }

    fun originalRank(player: Player?): Rank
    {
        player ?: return RankHandler.getDefaultRank()

        val lemonPlayer = PlayerHandler.findPlayer(player.uniqueId).orElse(null)

        return if (lemonPlayer != null)
        {
            lemonPlayer.activeGrant?.getRank() ?: RankHandler.getDefaultRank()
        } else
        {
            RankHandler.getDefaultRank()
        }
    }

    enum class MessageType
    {
        PLAYER_MESSAGE,
        NOTIFICATION
    }
}

infix fun Player.data(uuid: UUID): LemonPlayer?
{
    return PlayerHandler.findPlayer(uuid).orElse(null)
}
