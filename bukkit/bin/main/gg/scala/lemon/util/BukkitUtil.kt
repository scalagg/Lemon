package gg.scala.lemon.util

import net.evilblock.cubed.util.nms.MinecraftReflection
import java.lang.reflect.Field

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
object BukkitUtil {

    @JvmStatic
    val MINECRAFT_SERVER = MinecraftReflection.getMinecraftServer()

    @JvmStatic
    val PLAYER_LIST: Any = MINECRAFT_SERVER.javaClass
        .getMethod("getPlayerList")
        .invoke(MINECRAFT_SERVER)

    @JvmStatic
    val playerField: Field = PLAYER_LIST.javaClass
        .superclass.getDeclaredField("players")

    /**
     * Updates the minecraft server player list whilst
     * giving the user a chance to modify the list before it updates.
     */
    @JvmStatic
    @JvmOverloads
    @Suppress("UNCHECKED_CAST")
    fun updatePlayerList(
        lambda: (MutableList<Any>) -> Unit = {}
    ) {
        val players = playerField.get(PLAYER_LIST)
        lambda.invoke(players as MutableList<Any>)

        playerField.isAccessible = true
        playerField.set(PLAYER_LIST, players)
    }
}
