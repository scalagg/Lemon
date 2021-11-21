package gg.scala.lemon.disguise.update

import gg.scala.lemon.disguise.DisguiseProvider
import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
object DisguiseListener : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val lemonPlayer = PlayerHandler.findPlayer(event.player).orElse(null)

        if (lemonPlayer != null) {
            val metaData = lemonPlayer.getMetadata("disguised")

            if (metaData != null) {
                val disguiseInfo = Serializers.gson.fromJson(
                    metaData.asString(), DisguiseInfo::class.java
                )

                Tasks.asyncDelayed(2L) {
                    DisguiseProvider.handleDisguiseInternal(
                        player = event.player,
                        disguiseInfo = disguiseInfo,
                        connecting = true
                    )

                    event.player.sendMessage("${CC.SEC}You've been disguised as ${CC.PRI}${disguiseInfo.username}${CC.SEC}.")
                }
            }
        }
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        if (event.player.hasMetadata("disguised"))
        {
            DisguiseProvider.handleUnDisguise(
                player = event.player,
                suppressUnDisguiseEvent = true,
                sendNotification = false,
                callInternal = false
            )
        }
    }
}
