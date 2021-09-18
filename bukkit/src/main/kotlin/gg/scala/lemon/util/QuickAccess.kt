package gg.scala.lemon.util

import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RedisHandler
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.util.other.Cooldown
import gg.scala.lemon.util.other.FancyMessage
import me.lucko.helper.Events
import me.lucko.helper.terminable.composite.CompositeTerminable
import net.evilblock.cubed.nametag.NametagHandler
import net.evilblock.cubed.serializers.Serializers.gson
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX, puugz
 */
object QuickAccess {

    fun nameOrConsole(sender: CommandSender): String {
        if (sender is ConsoleCommandSender) {
            return "${CC.D_RED}Console"
        }

        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(sender as Player).orElse(null)

        lemonPlayer?.let {
            return it.getColoredName()
        } ?: return "${CC.D_RED}Console"
    }

    fun nameOrConsole(uuid: UUID?): String {
        uuid ?: return "${CC.D_RED}Console"

        return CubedCacheUtil.fetchName(uuid)!!
    }

    fun coloredName(name: String?): String? {
        val lemonPlayer = name?.let { Lemon.instance.playerHandler.findOnlinePlayer(it) }

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

    fun fetchColoredName(uuid: UUID?): String {
        uuid ?: return "${CC.D_RED}Console"

        val grants = Lemon.instance.grantHandler.fetchGrantsFor(uuid).get()

        val playerName = CubedCacheUtil.fetchName(uuid)
        val prominent = GrantRecalculationUtil.getProminentGrant(grants)
            ?: return Lemon.instance.rankHandler.getDefaultRank().color + playerName

        return prominent.getRank().color + playerName
    }

    fun fetchRankWeight(uuid: UUID?): CompletableFuture<Int> {
        return Lemon.instance.grantHandler.fetchGrantsFor(uuid).thenApplyAsync {
            val prominent = GrantRecalculationUtil.getProminentGrant(it) ?: return@thenApplyAsync 0

            return@thenApplyAsync prominent.getRank().weight
        }
    }

    fun coloredName(player: Player): String? {
        return coloredName(player.uniqueId)
    }

    fun reloadPlayer(uuid: UUID, recalculateGrants: Boolean = true) {
        Bukkit.getPlayer(uuid)?.let {
            Lemon.instance.playerHandler.findPlayer(it).ifPresent { lemonPlayer ->
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

    fun uuidOf(sender: CommandSender): UUID? {
        return if (sender is Player) {
            sender.uniqueId
        } else null
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
                put("sender-fancy", nameOrConsole(sender))
                put("message", message)
                put("permission", "lemon.staff")
                put("messageType", messageType.name)

                put("server", Lemon.instance.settings.id)
                put("with-server", addServer.toString())
            }
        ).publishAsync()
    }

    fun parseReason(
        reason: String?,
        fallback: String = "Unfair Advantage"
    ): String {
        var preParsedReason = reason?.removeSuffix("-s") ?: fallback
        preParsedReason = preParsedReason.removeSuffix(" ")

        return preParsedReason.ifBlank { fallback }
    }

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
            ).publishAsync()
        }
    }

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
                ).publishAsync()
            }

            false
        } else true
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalBroadcast(message: String, permission: String? = null): CompletableFuture<Void> {
        return RedisHandler.buildMessage(
            "global-message",
            buildMap {
                put("message", message)
                put("permission", permission ?: "")
            }
        ).publishAsync()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalFancyBroadcast(fancyMessage: FancyMessage, permission: String?): CompletableFuture<Void> {
        return RedisHandler.buildMessage(
            "global-fancy-message",
            buildMap {
                put("message", gson.toJson(fancyMessage))
                put("permission", permission ?: "")
            }
        ).publishAsync()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalPlayerMessage(message: String, uuid: UUID): CompletableFuture<Void> {
        return RedisHandler.buildMessage(
            "player-message",
            buildMap {
                put("message", message)
                put("target", uuid.toString())
            }
        ).publishAsync()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun sendGlobalPlayerFancyMessage(fancyMessage: FancyMessage, uuid: UUID): CompletableFuture<Void> {
        return RedisHandler.buildMessage(
            "player-fancy-message",
            buildMap {
                put("message", gson.toJson(fancyMessage))
                put("target", uuid.toString())
            }
        ).publishAsync()
    }

    fun startListening(itemStack: ItemStack, lambda: (PlayerInteractEvent) -> Unit) {
        Events.subscribe(PlayerInteractEvent::class.java)
            .filter { it.action.name.contains("RIGHT") }
            .filter { it.item != null && it.item.isSimilar(itemStack) }
            .handler(lambda)
    }

    fun startListening(itemStack: ItemStack, lambda: (PlayerInteractEvent) -> Unit, terminable: CompositeTerminable) {
        Events.subscribe(PlayerInteractEvent::class.java)
            .filter { it.action.name.contains("RIGHT") }
            .filter { it.item != null && it.item.isSimilar(itemStack) }
            .handler(lambda).bindWith(terminable)
    }

    fun startListeningAtEntity(itemStack: ItemStack, lambda: (PlayerInteractAtEntityEvent) -> Unit) {
        Events.subscribe(PlayerInteractAtEntityEvent::class.java)
            .filter { it.rightClicked is Player }
            .filter { it.player.itemInHand != null && it.player.itemInHand.isSimilar(itemStack) }
            .handler(lambda)
    }

    fun messageType(name: String): MessageType {
        return MessageType.valueOf(name)
    }

    fun weightOf(issuer: CommandSender): Int {
        if (issuer is ConsoleCommandSender) {
            return Int.MAX_VALUE
        }

        val lemonPlayer = Lemon.instance.playerHandler.findPlayer(issuer as Player).orElse(null)

        lemonPlayer?.let {
            return it.activeGrant!!.getRank().weight
        }

        return 0
    }

    enum class MessageType {
        PLAYER_MESSAGE,
        NOTIFICATION
    }
}
