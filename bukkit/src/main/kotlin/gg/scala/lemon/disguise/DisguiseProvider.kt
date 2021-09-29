package gg.scala.lemon.disguise

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import gg.scala.lemon.Lemon
import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.disguise.information.DisguiseInfoProvider
import gg.scala.lemon.disguise.information.DisguiseInfoProvider.useRandomAvailableDisguise
import gg.scala.lemon.disguise.update.event.PreDisguiseEvent
import gg.scala.lemon.disguise.update.event.UnDisguiseEvent
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.BukkitUtil
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.bukkit.Tasks.sync
import net.evilblock.cubed.util.nms.MinecraftReflection
import net.evilblock.cubed.visibility.VisibilityHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.io.InputStreamReader
import java.lang.reflect.Field
import java.net.URL
import java.util.*


/**
 * @author GrowlyX
 * @since 9/29/2021
 */
internal object DisguiseProvider {

    private val originalGameProfiles = mutableMapOf<UUID, Any>()
    private val uuidToDisguiseInfo = mutableMapOf<UUID, DisguiseInfo>()

    private val serverVersion = Bukkit.getServer().javaClass.getPackage()
        .name.replace(".", ",").split(",").toTypedArray()[3]

    private lateinit var entityGameProfileField: Field

    private val playOutPacketRespawn = MinecraftReflection.getNMSClass("PacketPlayOutRespawn")!!

    private val enumDifficulty = MinecraftReflection.getNMSClass("EnumDifficulty")!!
    private val enumGameMode = MinecraftReflection.getNMSClass("EnumGamemode")!!

    private val worldType = MinecraftReflection.getNMSClass("WorldType")!!
    private val worldTypeGetType = worldType.getMethod("getType", String::class.java)

    private val playOutPacketInfo = MinecraftReflection.getNMSClass("PacketPlayOutPlayerInfo")!!
    private val playOutPacketInfoAction = MinecraftReflection.getNMSClass("PacketPlayOutPlayerInfo$${"EnumPlayerInfoAction"}")!!

    private val playOutPacketInfoActionRemovePlayer = Reflection.getEnum(playOutPacketInfoAction, "REMOVE_PLAYER")
    private val playOutPacketInfoActionAddPlayer = Reflection.getEnum(playOutPacketInfoAction, "ADD_PLAYER")

    internal var initialized = false

    fun initialLoad() {
        val clazz = Class.forName(
            "org.bukkit.craftbukkit.$serverVersion.EntityHuman"
        )

        // newer versions have a different field
        // name, so this is why all this is here.
        val maxVersion = Bukkit.getServer().javaClass.getPackage().name.split("\\.")
            .toTypedArray()[3].replace("(v|R[0-9]+)".toRegex(), "").split("_").toTypedArray()[0].toInt()
        val minVersion = Bukkit.getServer().javaClass.getPackage().name.split("\\.")
            .toTypedArray()[3].replace("(v|R[0-9]+)".toRegex(), "").split("_").toTypedArray()[1].toInt()

        entityGameProfileField = if (maxVersion >= 1 && minVersion >= 9) {
            clazz.getDeclaredField("bS")
        } else {
            clazz.getDeclaredField("bH")
        }
        entityGameProfileField.isAccessible = true

        initialized = true
    }

    fun handleRandomDisguise(player: Player) {
        if (!initialized || !DisguiseInfoProvider.initialized) {
            // ACF will automatically catch this and re-throw
            // it while sending the player an error message.
            throw RuntimeException(
                "Disguise handlers have not been initialized properly."
            )
        }

        if (player.hasMetadata("disguised")) {
            throw ConditionFailedException("You're already disguised.")
        }

        useRandomAvailableDisguise { disguiseInfo ->
            if (disguiseInfo == null) {
                player.sendMessage("${CC.RED}No available disguise could be found for you.")
                return@useRandomAvailableDisguise
            }

            val preDisguiseEvent = PreDisguiseEvent(
                player, disguiseInfo
            )

            // as newer non-legacy versions require
            // events to be called on the main thread.
            sync {
                preDisguiseEvent.call()
            }

            if (preDisguiseEvent.isCancelled)
                return@useRandomAvailableDisguise

            handleDisguiseInternal(player, disguiseInfo)

            player.sendMessage("${CC.SEC}You've disguised yourself as ${CC.PRI}${disguiseInfo.username}${CC.SEC}.")
        }
    }

    fun handleUnDisguise(player: Player) {
        val disguiseInfo = uuidToDisguiseInfo[player.uniqueId]
            ?: throw ConditionFailedException("You're not currently disguised.")

        val unDisguiseEvent = UnDisguiseEvent(
            player, disguiseInfo
        )

        sync {
            unDisguiseEvent.call()
        }

        // players will be forced to disconnect or switch
        // to a separate server to un-disguise themselves.
        if (unDisguiseEvent.isCancelled)
            return

        handleUnDisguiseInternal(player, disguiseInfo)

        player.sendMessage("${CC.GREEN}You've undisguised.")
    }

    internal fun handleUnDisguiseInternal(
        player: Player, disguiseInfo: DisguiseInfo,
        disconnecting: Boolean = false
    ) {
        val originalGameProfile = originalGameProfiles[player.uniqueId]
        val handle = MinecraftReflection.getHandle(player)

        originalGameProfiles.remove(player.uniqueId)
        uuidToDisguiseInfo.remove(player.uniqueId)

        entityGameProfileField.set(handle, originalGameProfile)

        val lemonPlayer = PlayerHandler.findPlayer(player)
            .orElse(null)

        player.removeMetadata(
            "disguised", Lemon.instance
        )

        if (!disconnecting) {
            lemonPlayer.removeMetadata("disguised")

            reloadPlayerInternal(player, handle)
        }

        DisguiseInfoProvider.activeDisguises.deleteEntry(
            disguiseInfo.uuid.toString()
        )
    }

    internal fun handleDisguiseInternal(
        player: Player, disguiseInfo: DisguiseInfo,
        connecting: Boolean = false
    ) {
        val gameProfile = GameProfile(
            // we're not going to change the player's original
            // uuid as if we did, it would cause a toon of issues.
            player.uniqueId, disguiseInfo.username
        )
        val handle = MinecraftReflection.getHandle(player)
        val originalGameProfile = MinecraftReflection.getGameProfile(player)

        // updating the new game profile's skin info
        gameProfile.properties.removeAll("textures")
        gameProfile.properties.put(
            "textures",
            Property(
                "textures",
                disguiseInfo.skinInfo, disguiseInfo.skinSignature
            )
        )

        originalGameProfiles[player.uniqueId] = originalGameProfile
        uuidToDisguiseInfo[player.uniqueId] = disguiseInfo

        entityGameProfileField.set(handle, gameProfile)

        player.setMetadata(
            "disguised",
            FixedMetadataValue(Lemon.instance, true)
        )

        if (!connecting) {
            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

            lemonPlayer.updateOrAddMetadata(
                "disguised",
                Metadata(Serializers.gson.toJson(disguiseInfo))
            )
        }

        sync {
            reloadPlayerInternal(player, handle)
        }

        DisguiseInfoProvider.activeDisguises.saveEntry(
            disguiseInfo.uuid.toString(), disguiseInfo
        )
    }

    private fun reloadPlayerInternal(
        player: Player, handle: Any
    ) {
        VisibilityHandler.updateToAll(player)
        QuickAccess.reloadPlayer(player.uniqueId)

        val playerConnection = handle.javaClass
            .getField("playerConnection").get(handle)

        val sendPacketMethod =  playerConnection.javaClass.getMethod("sendPacket")

        val previousLocation = player.location.clone()

        val addPlayerPacket = Reflection.callConstructor(
            playOutPacketInfo, playOutPacketInfoActionAddPlayer, handle
        )
        val removePlayerPacket = Reflection.callConstructor(
            playOutPacketInfo, playOutPacketInfoActionRemovePlayer, handle
        )
        val respawnPacket = Reflection.callConstructor(
            playOutPacketRespawn,
            player.world.environment.id,

            Reflection.getEnum(
                enumDifficulty, player.world.difficulty.name
            ),
            worldTypeGetType.invoke(
                player.world.worldType.name
            ),
            Reflection.getEnum(
                enumGameMode, player.gameMode.name
            ),
        )

        listOf(addPlayerPacket, removePlayerPacket, respawnPacket).forEach {
            sendPacketMethod.invoke(playerConnection, it)
        }

        BukkitUtil.updatePlayerList {
            it.remove(handle)
            it.add(MinecraftReflection.getHandle(player))
        }

        player.inventory.armorContents = player.inventory.armorContents
        player.inventory.contents = player.inventory.contents

        player.health = player.health
        player.foodLevel = player.foodLevel
        player.level = player.level
        player.exp = player.exp
        player.fireTicks = 0
        player.maximumNoDamageTicks = player.maximumNoDamageTicks
        player.saturation = player.saturation
        player.allowFlight = player.allowFlight
        player.flySpeed = player.flySpeed
        player.gameMode = player.gameMode

        player.inventory.itemInHand = player.itemInHand
        player.updateInventory()

        player.teleport(previousLocation)
    }

    fun fetchDisguiseInfo(name: String, uuid: UUID): DisguiseInfo? {
        return try {
            val url = URL(
                "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString()
                    .replace("-", "") + "?unsigned=false"
            )

            val json = JsonParser().parse(InputStreamReader(url.openStream())).asJsonObject.get("properties")
                    .asJsonArray.get(0).asJsonObject

            val skin: String = json.get("value").asString
            val signature: String = json.get("signature").asString

            DisguiseInfo(uuid, name, skin, signature)
        } catch (exception: Exception) {
            null
        }
    }
}
