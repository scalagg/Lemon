package com.solexgames.lemon.listener

import com.mongodb.client.model.Filters
import com.solexgames.lemon.Lemon
import com.solexgames.lemon.player.LemonPlayer
import me.lucko.helper.Events
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.concurrent.CompletableFuture

class PlayerListener(private val plugin: Lemon) : Listener {

    fun registerHelperEvents() {
        Events.subscribe(AsyncPlayerPreLoginEvent::class.java, EventPriority.HIGHEST).handler {
            val lemonPlayer = LemonPlayer(
                it.uniqueId, it.name, it.address.hostAddress
            )

            val completableFuture = CompletableFuture.supplyAsync {
                this.plugin.mongoHandler.playerCollection.find(
                    Filters.eq(
                        "uuid",
                        it.uniqueId.toString()
                    )
                ).first()
            }

            lemonPlayer.load(completableFuture)

            this.plugin.playerHandler.players[it.uniqueId] = lemonPlayer
        }

        Events.subscribe(PlayerQuitEvent::class.java).handler {
            val lemonPlayer = this.plugin.playerHandler.getPlayer(it.player)

            lemonPlayer.ifPresent { player ->
                player.save().whenComplete { _, u ->
                    u?.printStackTrace()
                }
            }
        }
    }
}
