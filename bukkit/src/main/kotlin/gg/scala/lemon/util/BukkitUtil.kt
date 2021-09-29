package gg.scala.lemon.util

import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.Bukkit

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
object BukkitUtil {

    /**
     * Updates the minecraft server player list whilst
     * giving the user a chance to modify the list before it updates.
     */
    @JvmStatic
    fun updatePlayerList(
        lambda: (MutableList<Any>) -> Unit = {}
    ) {
        val minecraftServer = MinecraftReflection.getMinecraftServer()

        val players = Bukkit.getOnlinePlayers()
            .mapNotNull { PlayerHandler.findPlayer(it).orElse(null) }
            .sortedByDescending { QuickAccess.realRank(it.bukkitPlayer!!).weight }
            .map { MinecraftReflection.getHandle(it.bukkitPlayer!!) }

        val mutableList = mutableListOf(*players.toTypedArray())
        lambda.invoke(mutableList)

        val playerList = minecraftServer.javaClass
            .getMethod("getPlayerList")
            .invoke(minecraftServer)

        val field = playerList.javaClass
            .superclass.getDeclaredField("players")

        field.isAccessible = true
        field.set(playerList, mutableList.toList())
    }
}
