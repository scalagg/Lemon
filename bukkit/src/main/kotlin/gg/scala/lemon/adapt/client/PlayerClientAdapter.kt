package gg.scala.lemon.adapt.client

import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
interface PlayerClientAdapter {

    fun getClientName(): String

    fun shouldHandle(player: Player): Boolean

    fun enableStaffModules(player: Player)
    fun disableStaffModules(player: Player)

    fun sendTitle(player: Player, title: String, subtitle: String)

}