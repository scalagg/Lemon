package gg.scala.lemon.util

import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue

/**
 * @author GrowlyX
 * @since 9/27/2021
 */
object BatUtil {

    @JvmStatic private val entityBatClass = MinecraftReflection.getNMSClass("EntityBat")!!

    @JvmStatic private val spawnEntityPacket = MinecraftReflection.getNMSClass("PacketPlayOutSpawnEntityLiving")!!
    @JvmStatic private val attachPacket = MinecraftReflection.getNMSClass("PacketPlayOutAttachEntity")!!
    @JvmStatic private val destroyPacket = MinecraftReflection.getNMSClass("PacketPlayOutEntityDestroy")!!

    @JvmStatic private val entityBatSetLocation = entityBatClass.getMethod("setLocation")
    @JvmStatic private val entityBatSetVisibility = entityBatClass.getMethod("setInvisible")
    @JvmStatic private val entityBatSetHealth = entityBatClass.getMethod("setHealth")
    @JvmStatic private val entityBatId = entityBatClass.getMethod("getId")

    /**
     * Attaches a player onto a bat via reflection.
     *
     * @author GrowlyX
     *
     * @see [Reflection]
     * @see [MinecraftReflection]
     */
    @JvmStatic
    fun sitOnBat(player: Player) {
        val craftPlayer = MinecraftReflection.getHandle(player)
        val entityBat = entityBatClass.newInstance()

        entityBatSetLocation
            .invoke(entityBat, player.location.x, player.location.y + 0.5, player.location.z, 0, 0)

        entityBatSetVisibility.invoke(entityBat, true)
        entityBatSetHealth.invoke(entityBat, 6)

        val spawnEntityPacket = Reflection
            .callConstructor(spawnEntityPacket, entityBat)

        val attachPacket = Reflection
            .callConstructor(attachPacket, 0, craftPlayer, entityBat)

        val playerConnection = craftPlayer.javaClass
            .getField("playerConnection").get(craftPlayer)

        playerConnection.javaClass.getMethod("sendPacket")
            .invoke(playerConnection, spawnEntityPacket)

        playerConnection.javaClass.getMethod("sendPacket")
            .invoke(playerConnection, attachPacket)

        player.setMetadata(
            "seated",
            FixedMetadataValue(
                Lemon.instance,
                entityBatId.invoke(entityBat)
            )
        )
    }

    /**
     * Make a player un sit on a bat via reflection.
     *
     * @author GrowlyX, Nv6 (thought of name of method)
     */
    @JvmStatic
    fun makePlayerUnSitOnBat(player: Player) {
        if (player.hasMetadata("seated")) {
            val craftPlayer = MinecraftReflection.getHandle(player)

            val playerConnection = craftPlayer.javaClass
                .getField("playerConnection").get(craftPlayer)

            val metaData = player.getMetadata("seated")[0].asInt()

            val destroyPacket = Reflection.callConstructor(
                destroyPacket, metaData
            )

            playerConnection.javaClass.getMethod("sendPacket")
                .invoke(playerConnection, destroyPacket)
        }
    }
}
