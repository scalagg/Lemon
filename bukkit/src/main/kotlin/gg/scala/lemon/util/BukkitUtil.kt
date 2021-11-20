package gg.scala.lemon.util

import gg.scala.lemon.handler.PlayerHandler
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.Bukkit
import java.lang.reflect.Field

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
object BukkitUtil {

    @JvmStatic
    val minecraftServer = MinecraftReflection.getMinecraftServer()

    @JvmStatic
    val playerList: Any = minecraftServer.javaClass
        .getMethod("getPlayerList")
        .invoke(minecraftServer)

    @JvmStatic
    val playerField: Field = playerList.javaClass
        .superclass.getDeclaredField("players")

    /**
     * Updates the minecraft server player list whilst
     * giving the user a chance to modify the list before it updates.
     */
    @JvmStatic
    fun updatePlayerList(
        lambda: (MutableList<Any>) -> Unit = {}
    ) {
//        val players = Bukkit.getOnlinePlayers()
//            .map { PlayerHandler.findPlayer(it).orElse(null) }
//            .sortedByDescending {  }
//            .map { MinecraftReflection.getHandle(it.bukkitPlayer!!) }
//
//        playerField.isAccessible = true
//        playerField.set(playerList, players)
    }
}
