package gg.scala.lemon.util

import gg.scala.banana.message.Message
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.adapt.client.PlayerClientAdapter
import gg.scala.lemon.handler.*
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.util.other.Cooldown
import gg.scala.lemon.util.other.FancyMessage
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.Serializers.gson
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool

/**
 * @author GrowlyX, puugz
 */
object QuickAccess {

    @JvmStatic
    fun nameOrConsole(sender: CommandSender): String {
        if (sender is ConsoleCommandSender) {
            return LemonConstants.CONSOLE
        }

        val lemonPlayer = PlayerHandler.findPlayer(sender as Player).orElse(null)

        lemonPlayer?.let {
            return it.getColoredName()
        } ?: return LemonConstants.CONSOLE
    }

    @JvmStatic
    fun nameOrConsole(uuid: UUID?): String {
        uuid ?: return LemonConstants.CONSOLE

        return CubedCacheUtil.fetchName(uuid)!!
    }

    @JvmStatic
    fun coloredName(name: String?): String? {
        val lemonPlayer = name?.let { PlayerHandler.findOnlinePlayer(it) }

        lemonPlayer?.let {
            return it.getColoredName()
        } ?: return name
    }

    @JvmStatic
    fun coloredName(uuid: UUID): String? {
        val lemonPlayer = PlayerHandler.findPlayer(uuid).orElse(null)

        lemonPlayer?.let {
            return it.getColoredName()
        }  ?: return null
    }

    @JvmStatic
    fun fetchColoredName(uuid: UUID?): String {
        uuid ?: return LemonConstants.CONSOLE

        val grants = GrantHandler.fetchGrantsFor(uuid).get()

        val playerName = CubedCacheUtil.fetchName(uuid)
        val prominent = GrantRecalculationUtil.getProminentGrant(grants)
            ?: return RankHandler.getDefaultRank().color + playerName

        return prominent.getRank().color + playerName
    }

    @JvmStatic
    fun fetchRankWeight(uuid: UUID?): CompletableFuture<Int> {
        return GrantHandler.fetchGrantsFor(uuid).thenApplyAsync {
            val prominent = GrantRecalculationUtil.getProminentGrant(it) ?: return@thenApplyAsync 0

            return@thenApplyAsync prominent.getRank().weight
        }
    }

    @JvmStatic
    fun fetchIpAddress(uuid: UUID?): CompletableFuture<String?> {
        return DataStoreHandler.lemonPlayerLayer.fetchEntryByKey(uuid.toString()).thenApply {
            return@thenApply it.previousIpAddress
        }
    }

    @JvmStatic
    fun coloredName(player: Player): String? {
        return coloredName(player.uniqueId)
    }

    @JvmStatic
    fun reloadPlayer(uuid: UUID, recalculateGrants: Boolean = true) {
        Bukkit.getPlayer(uuid)?.let {
            PlayerHandler.findPlayer(it).ifPresent { lemonPlayer ->
                NametagHandler.reloadPlayer(it)
                VisibilityHandler.update(it)

                if (recalculateGrants) {
                    lemonPlayer.recalculateGrants(
                        shouldCalculateNow = true
                    )
                }

                it.displayName = lemonPlayer.getColoredName()

                lemonPlayer.pushCocoaUpdates()
            }
        }
    }

    @JvmStatic
    fun uuidOf(sender: CommandSender): UUID? {
        return if (sender is Player) {
            sender.uniqueId
        } else null
    }

    @JvmStatic
    fun remaining(cooldown: Cooldown): String {
        return String.format("%.0f", (cooldown.getRemaining() / 1000).toFloat())
    }

    @JvmStatic
    fun replaceEmpty(string: String): String {
        return ChatColor.stripColor(string).ifBlank {
            "${CC.RED}None"
        }
    }

    @JvmStatic
    fun senderUuid(sender: CommandSender): UUID? {
        return if (sender is ConsoleCommandSender) null else (sender as Player).uniqueId
    }

    @JvmStatic
    @OptIn(ExperimentalStdlibApi::class)
    fun sendStaffMessage(
        sender: CommandSender?,
        message: String,
        addServer: Boolean,
        messageType: MessageType
    ): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "staff-message",
                buildMap {
                    put("sender-fancy", sender?.let { nameOrConsole(it) } ?: "")
                    put("message", message)
                    put("permission", "lemon.staff")
                    put("message-type", messageType.name)

                    put("server", Lemon.instance.settings.id)
                    put("with-server", addServer.toString())
                }
            ).dispatchToLemon()
        }
    }

    @JvmStatic
    @OptIn(ExperimentalStdlibApi::class)
    fun sendStaffMessageWithFlag(
        sender: CommandSender?,
        message: String,
        addServer: Boolean,
        messageType: MessageType,
        flag: String
    ): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "staff-message",
                buildMap {
                    put("sender-fancy", sender?.let { nameOrConsole(it) } ?: "")
                    put("message", message)
                    put("permission", "lemon.staff")
                    put("message-type", messageType.name)
                    put("flag", flag)

                    put("server", Lemon.instance.settings.id)
                    put("with-server", addServer.toString())
                }
            ).dispatchToLemon()
        }
    }

    @JvmStatic
    fun parseReason(
        reason: String?,
        fallback: String = "Unfair Advantage"
    ): String {
        var preParsedReason = reason?.removeSuffix("-s") ?: fallback
        preParsedReason = preParsedReason.removeSuffix(" ")

        return preParsedReason.ifBlank { fallback }
    }

    @JvmStatic
    fun attemptRemoval(
        punishment: Punishment,
        reason: String = "Expired",
        remover: UUID? = null
    ) {
        punishment.isRemoved = true
        punishment.removedAt = System.currentTimeMillis()
        punishment.removedOn = Lemon.instance.settings.id
        punishment.removedBy = remover
        punishment.removedReason = reason

        punishment.save().thenAccept {
            RedisHandler.buildMessage(
                "recalculate-punishments",
                mutableMapOf<String, String>().also { map ->
                    map["uniqueId"] = punishment.target.toString()
                }
            ).dispatchToLemon()
        }
    }

    @JvmStatic
    fun attemptExpiration(punishment: Punishment, reason: String = "Expired", remover: UUID? = null) : Boolean {
        return if (!punishment.isRemoved && punishment.hasExpired && !punishment.category.instant) {
            punishment.isRemoved = true
            punishment.removedAt = System.currentTimeMillis()
            punishment.removedOn = Lemon.instance.settings.id
            punishment.removedBy = remover
            punishment.removedReason = reason

            punishment.save().thenRun {
                RedisHandler.buildMessage(
                    "recalculate-punishments",
                    mutableMapOf<String, String>().also { map ->
                        map["uniqueId"] = punishment.target.toString()
                    }
                ).dispatchToLemon()
            }

            false
        } else true
    }

    @JvmStatic
    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalBroadcast(message: String, permission: String? = null): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "global-message",
                buildMap {
                    put("message", message)
                    put("permission", permission ?: "")
                }
            ).dispatchToLemon()
        }
    }

    @JvmStatic
    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalFancyBroadcast(fancyMessage: FancyMessage, permission: String?): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "global-fancy-message",
                buildMap {
                    put("message", gson.toJson(fancyMessage))
                    put("permission", permission ?: "")
                }
            ).dispatchToLemon()
        }
    }

    @JvmStatic
    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalPlayerMessage(message: String, uuid: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "player-message",
                buildMap {
                    put("message", message)
                    put("target", uuid.toString())
                }
            ).dispatchToLemon()
        }
    }

    @JvmStatic
    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalPlayerFancyMessage(fancyMessage: FancyMessage, uuid: UUID): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            RedisHandler.buildMessage(
                "player-fancy-message",
                buildMap {
                    put("message", gson.toJson(fancyMessage))
                    put("target", uuid.toString())
                }
            ).dispatchToLemon()
        }
    }

    @JvmStatic
    fun messageType(name: String): MessageType {
        return MessageType.valueOf(name)
    }

    @JvmStatic
    fun weightOf(issuer: CommandSender): Int {
        if (issuer is ConsoleCommandSender) {
            return Int.MAX_VALUE
        }

        val lemonPlayer = PlayerHandler.findPlayer(issuer as Player).orElse(null)

        lemonPlayer?.let {
            return it.activeGrant!!.getRank().weight
        }

        return 0
    }

    @JvmStatic
    fun shouldBlock(player: Player): Boolean {
        val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

        if (!lemonPlayer.hasPermission("lemon.2fa.forced")) return false
        if (lemonPlayer.isAuthExempt()) return false
        if (lemonPlayer.hasAuthenticatedThisSession()) return false

        return true
    }

    enum class MessageType {
        PLAYER_MESSAGE,
        NOTIFICATION
    }
}

fun Message.dispatchToLemon() {
    ForkJoinPool.commonPool().execute {
        Lemon.instance.banana.useResource {
            it.publish("lemon:spigot", gson.toJson(this))
            it.close()
        }
    }
}

fun Message.dispatchToCocoa() {
    ForkJoinPool.commonPool().execute {
        Lemon.instance.banana.useResource {
            it.publish("cocoa", gson.toJson(this))
            it.close()
        }
    }
}
