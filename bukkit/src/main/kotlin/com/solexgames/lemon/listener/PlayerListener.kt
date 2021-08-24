package com.solexgames.lemon.listener

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.LemonPlayer
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture

class PlayerListener(private val plugin: Lemon) : Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val lemonPlayer = Lemon.instance.playerHandler.getPlayer(player).orElse(null)
        val serverHandler = Lemon.instance.serverHandler

        if (!lemonPlayer.isStaff()) {
            if (serverHandler.chatMuted || lemonPlayer.hasPermission("lemon.chat.bypass")) {
                event.isCancelled = true
                player.sendMessage("${CC.RED}Global chat is currently muted.")
            }
//            else if (player is muted) {
//                event.isCancelled = true
//                player.sendMessage("${CC.RED}You're muted for ...")
//            }

        }
    }

    fun registerHelperEvents() {
        Events.subscribe(AsyncPlayerPreLoginEvent::class.java, EventPriority.HIGHEST).handler {
            val lemonPlayer = LemonPlayer(
                it.uniqueId, it.name, it.address.hostAddress
            )

            val completableFuture = CompletableFuture.supplyAsync {
                plugin.mongoHandler.playerCollection.find(
                    Filters.eq(
                        "uuid",
                        it.uniqueId.toString()
                    )
                ).first()
            }

            lemonPlayer.load(completableFuture)

            plugin.playerHandler.players[it.uniqueId] = lemonPlayer
        }

        Events.subscribe(PlayerQuitEvent::class.java).handler {
            val lemonPlayer = plugin.playerHandler.getPlayer(it.player)

            lemonPlayer.ifPresent { player ->
                player.save().whenComplete { _, u ->
                    u?.printStackTrace()
                }
            }
        }
    }
}
