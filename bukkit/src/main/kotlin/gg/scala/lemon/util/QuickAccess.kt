package gg.scala.lemon.util

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.GrantHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.minequest
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
@Service(name = "LemonQuickAccess")
object QuickAccess
{
    private val connection by lazy {
        Lemon.instance.aware.internal().connect()
    }

    @JvmStatic
    fun UUID.username(): String =
        CubedCacheUtil.fetchName(this)!!

    @JvmStatic
    fun String.uniqueId(): UUID =
        CubedCacheUtil.fetchUuid(this)!!

    @Configure
    fun configure()
    {
        this.connection
    }

    @JvmStatic
    fun getLastOnline(player: UUID): CompletableFuture<Long?>
    {
        return CompletableFuture.supplyAsync {
            this.connection.sync()
                .hget(
                    "player:$player",
                    "lastOnline"
                )
                ?.toLong()
                ?: return@supplyAsync null
        }
    }

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
    fun coloredNameOrNull(name: String, ignoreMinequest: Boolean = false): String?
    {
        return PlayerHandler.findPlayer(name)
            .orElse(null)
            ?.getOriginalColoredName(
                ignoreMinequest = ignoreMinequest
            )
    }

    @JvmStatic
    fun coloredName(uuid: UUID, ignoreMinequest: Boolean = false): String?
    {
        val lemonPlayer = PlayerHandler.findPlayer(uuid).orElse(null)

        lemonPlayer?.let {
            return it.getColoredName(ignoreMinequest = ignoreMinequest)
        } ?: return null
    }

    @JvmStatic
    fun sendChannelMessage(
        channelId: String,
        message: String,
        sender: LemonPlayer
    )
    {
        RedisHandler.buildMessage(
            "channel-message",
            "channel" to channelId,
            "message" to message,
            "sender" to sender.uniqueId,
            "rank" to sender.activeGrant!!
                .getRank().uuid.toString(),
            "server" to Lemon.instance
                .settings.id
        ).publish()
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
    fun coloredName(player: Player, ignoreMinequest: Boolean = false): String?
    {
        return coloredName(player.uniqueId, ignoreMinequest = ignoreMinequest)
    }

    @JvmStatic
    fun reloadPlayer(uuid: UUID, recalculateGrants: Boolean = true)
    {
        Bukkit.getPlayer(uuid)?.let {
            PlayerHandler.findPlayer(it).ifPresent { lemonPlayer ->
                it.displayName = lemonPlayer.getColoredName()

                it.playerListName = lemonPlayer
                    .getColoredName(
                        customColor = false,
                        prefixIncluded = minequest()
                    )

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
        val empty = ChatColor
            .stripColor(string)
            .isBlank()

        if (empty)
        {
            return "${CC.RED}None"
        }

        return string
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
        message: String,
        addServer: Boolean
    ): CompletableFuture<Void>
    {
        RedisHandler.buildMessage(
            "staff-message",
            "message" to message,
            "permission" to "lemon.staff",
            "server" to Lemon.instance.settings.id,
            "with-server" to addServer
        ).publish()

        return CompletableFuture
            .completedFuture(null)
    }

    @JvmStatic
    fun sendStaffMessageWithFlag(
        message: String,
        addServer: Boolean,
        flag: String
    ): CompletableFuture<Void>
    {
        RedisHandler.buildMessage(
            "staff-message",
            "message" to message,
            "permission" to "lemon.staff",
            "flag" to flag,
            "server" to Lemon.instance.settings.id,
            "with-server" to addServer
        ).publish()

        return CompletableFuture
            .completedFuture(null)
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
        punishment.removedAt = System.currentTimeMillis()
        punishment.removedOn = Lemon.instance.settings.id
        punishment.removedBy = remover
        punishment.removedReason = reason

        punishment.save().thenAccept {
            RedisHandler.buildMessage(
                "recalculate-punishments",
                "uniqueId" to punishment.target.toString()
            ).publish()
        }
    }

    @JvmStatic
    fun attemptExpiration(punishment: Punishment, reason: String = "Expired", remover: UUID? = null): Boolean
    {
        return if (!punishment.isRemoved && punishment.hasExpired && !punishment.category.instant)
        {
            punishment.removedAt = System.currentTimeMillis()
            punishment.removedOn = Lemon.instance.settings.id
            punishment.removedBy = remover
            punishment.removedReason = reason

            punishment.save().thenRun {
                RedisHandler.buildMessage(
                    "recalculate-punishments",
                    "uniqueId" to punishment.target.toString()
                ).publish()
            }

            false
        } else true
    }

    @JvmStatic
    fun sendGlobalBroadcast(
        message: String,
        permission: String? = null
    ): CompletableFuture<Void>
    {
        RedisHandler.buildMessage(
            "global-message",
            "message" to message,
            "permission" to permission
        ).publish()

        return CompletableFuture
            .completedFuture(null)
    }

    @JvmStatic
    fun sendGlobalFancyBroadcast(
        fancyMessage: FancyMessage,
        permission: String?,
        metaPermission: String? = null
    ): CompletableFuture<Void>
    {
        RedisHandler.buildMessage(
            "global-fancy-message",
            "message" to gson.toJson(fancyMessage),
            "permission" to permission,
            "meta-permission" to metaPermission,
        ).publish()

        return CompletableFuture
            .completedFuture(null)
    }

    @JvmStatic
    fun sendGlobalPlayerMessage(message: String, uuid: UUID): CompletableFuture<Void>
    {
        RedisHandler.buildMessage(
            "player-message",
            "message" to message,
            "target" to uuid.toString()
        ).publish()

        return CompletableFuture
            .completedFuture(null)
    }

    @JvmStatic
    fun sendGlobalPlayerFancyMessage(fancyMessage: FancyMessage, uuid: UUID): CompletableFuture<Void>
    {
        RedisHandler.buildMessage(
            "player-fancy-message",
            "message" to gson.toJson(fancyMessage),
            "target" to uuid.toString()
        ).publish()

        return CompletableFuture
            .completedFuture(null)
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
