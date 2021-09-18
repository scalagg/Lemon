package gg.scala.lemon.player.channel

import org.bukkit.entity.Player

/**
 * @author puugz
 * @since 27/08/2021 20:25
 */
interface ChannelOverride: Channel {

    fun getWeight(): Int

    fun shouldOverride(player: Player): Boolean

}
