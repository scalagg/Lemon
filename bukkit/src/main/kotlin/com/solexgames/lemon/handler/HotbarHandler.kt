package com.solexgames.lemon.handler

import me.lucko.helper.Events
import net.evilblock.cubed.util.bukkit.player.PlayerSnapshot
import net.evilblock.cubed.util.bukkit.selection.impl.ItemInteractionHandler
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import kotlin.collections.HashMap

/**
 * @author puugz
 * @since 28/08/2021 14:00
 */
object HotbarHandler {

    val snapshots = HashMap<UUID, PlayerSnapshot>()

    init {
        Events.subscribe(PlayerQuitEvent::class.java, EventPriority.HIGHEST)
            .handler {
                snapshots[it.player.uniqueId]?.restore(it.player, false)
            }
    }

    fun applyModModeHotbar(player: Player) {
        snapshots[player.uniqueId] = PlayerSnapshot(player)

        // use ingot for hotbar
    }
}