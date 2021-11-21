package gg.scala.lemon.disguise

import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import gg.scala.lemon.Lemon
import gg.scala.lemon.disguise.information.DisguiseInfo
import gg.scala.lemon.disguise.information.DisguiseInfoProvider
import gg.scala.lemon.disguise.information.DisguiseInfoProvider.useRandomAvailableDisguise
import gg.scala.lemon.disguise.update.event.PostDisguiseEvent
import gg.scala.lemon.disguise.update.event.PreDisguiseEvent
import gg.scala.lemon.disguise.update.event.UnDisguiseEvent
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.player.sorter.ScalaSpigotSorterExtension
import gg.scala.lemon.util.BukkitUtil
import gg.scala.lemon.util.QuickAccess
import me.lucko.helper.Events
import net.evilblock.cubed.acf.ConditionFailedException
import net.evilblock.cubed.entity.npc.protocol.NpcProtocol
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.bukkit.Tasks.sync
import net.evilblock.cubed.util.nms.MinecraftProtocol
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import java.io.InputStreamReader
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.net.URL
import java.util.*

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
internal object DisguiseProvider
{

    internal val originalGameProfiles = mutableMapOf<UUID, Any>()
    internal val uuidToDisguiseInfo = mutableMapOf<UUID, DisguiseInfo>()

    private val serverVersion = Bukkit.getServer().javaClass.getPackage()
        .name.replace(".", ",").split(",").toTypedArray()[3]

    private lateinit var entityGameProfileField: Field

    private val enumDifficulty = MinecraftReflection.getNMSClass("EnumDifficulty")!!
    private val enumGameMode = MinecraftReflection.getNMSClass("WorldSettings$${"EnumGamemode"}")!!

    private val worldType = MinecraftReflection.getNMSClass("WorldType")!!

    private val ENUM_GAME_MODE_CLASS: Class<*> = MinecraftReflection.getNMSClass("WorldSettings\$EnumGamemode")!!
    private val I_CHAT_BASE_COMPONENT_CLASS: Class<*> = MinecraftReflection.getNMSClass("IChatBaseComponent")!!

    private val PACKET_PLAY_OUT_PLAYER_INFO_CLASS: Class<*> =
        MinecraftReflection.getNMSClass("PacketPlayOutPlayerInfo")!!

    private val PLAYER_INFO_DATA_CLASS: Class<*> =
        PACKET_PLAY_OUT_PLAYER_INFO_CLASS.declaredClasses.find { it.simpleName == "PlayerInfoData" }!!

    private val PLAYER_INFO_DATA_CONSTRUCTOR: Constructor<*> = Reflection.getDeclaredConstructor(
        PLAYER_INFO_DATA_CLASS,

        PACKET_PLAY_OUT_PLAYER_INFO_CLASS,
        MinecraftReflection.getGameProfileClass(),
        Int::class.java,
        ENUM_GAME_MODE_CLASS,
        I_CHAT_BASE_COMPONENT_CLASS
    )!!

    private var initialized = false

    fun initialLoad()
    {
        val clazz = Class.forName(
            "net.minecraft.server.$serverVersion.EntityHuman"
        )

        val version = Bukkit.getServer().javaClass.getPackage().name
            .replace(".", ",").split(",").toTypedArray()[3]

        val subVersion = version.replace("v1_", "")
            .replace("_R\\d".toRegex(), "").toInt()

        entityGameProfileField = if (subVersion >= 9)
        {
            clazz.getDeclaredField("bS")
        } else
        {
            clazz.getDeclaredField("bH")
        }
        entityGameProfileField.isAccessible = true

        initialized = true
    }

    fun handleRandomDisguise(player: Player)
    {
        if (!initialized || !DisguiseInfoProvider.initialized)
        {
            // ACF will automatically catch this and re-throw
            // it while sending the player an error message.
            throw RuntimeException(
                "Disguise handlers have not been initialized properly."
            )
        }

        if (player.hasMetadata("disguised"))
        {
            throw ConditionFailedException("You're already disguised.")
        }

        useRandomAvailableDisguise { disguiseInfo ->
            if (disguiseInfo == null)
            {
                player.sendMessage("${CC.RED}No available disguise could be found for you.")
                return@useRandomAvailableDisguise
            }

            val preDisguiseEvent = PreDisguiseEvent(
                player, disguiseInfo
            )

            sync {
                preDisguiseEvent.call()
            }

            if (preDisguiseEvent.isCancelled)
                return@useRandomAvailableDisguise

            handleDisguiseInternal(player, disguiseInfo)

            sync {
                PostDisguiseEvent(player).call()
            }

            player.sendMessage("${CC.SEC}You've disguised yourself as ${CC.PRI}${disguiseInfo.username}${CC.SEC}. ${CC.GRAY}(with a random skin)")
        }
    }

    fun handleUnDisguise(player: Player, callInternal: Boolean = true, sendNotification: Boolean = true, suppressUnDisguiseEvent: Boolean = false)
    {
        val disguiseInfo = uuidToDisguiseInfo[player.uniqueId]
            ?: throw ConditionFailedException("You're not currently disguised.")

        if (!suppressUnDisguiseEvent)
        {
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
        }

        if (callInternal)
            handleUnDisguiseInternal(player, disguiseInfo)

        if (sendNotification)
            player.sendMessage("${CC.GREEN}You've undisguised yourself.")
    }

    internal fun handleUnDisguiseInternal(
        player: Player, disguiseInfo: DisguiseInfo,
        disconnecting: Boolean = false
    )
    {
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

        if (!disconnecting)
        {
            lemonPlayer remove "disguised"

            reloadPlayerInternal(player, handle)
        }

        DisguiseInfoProvider.activeDisguises.deleteEntry(
            disguiseInfo.uuid.toString()
        )
    }

    internal fun handleDisguiseInternal(
        player: Player, disguiseInfo: DisguiseInfo,
        connecting: Boolean = false
    )
    {
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

        if (!connecting)
        {
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
    )
    {
        val previousLocation = player.location.clone()
        val gameMode = Reflection.getEnum(
            enumGameMode, player.gameMode.name
        )

        val playerInfoAddPacket = MinecraftProtocol.newPacket("PacketPlayOutPlayerInfo")
        Reflection.setDeclaredFieldValue(
            playerInfoAddPacket,
            "a",
            Reflection.getEnum(NpcProtocol.ENUM_PLAYER_INFO_ACTION_CLASS, "ADD_PLAYER")!!
        )
        Reflection.setDeclaredFieldValue(
            playerInfoAddPacket, "b", arrayListOf(
                PLAYER_INFO_DATA_CONSTRUCTOR.newInstance(
                    playerInfoAddPacket,
                    MinecraftReflection.getGameProfile(player),
                    MinecraftReflection.getPing(player),
                    gameMode,
                    handle.javaClass.getField("listName").get(handle)
                )
            )
        )

        val playerInfoRemovePacket = MinecraftProtocol.newPacket("PacketPlayOutPlayerInfo")
        Reflection.setDeclaredFieldValue(
            playerInfoRemovePacket,
            "a",
            Reflection.getEnum(NpcProtocol.ENUM_PLAYER_INFO_ACTION_CLASS, "REMOVE_PLAYER")!!
        )
        Reflection.setDeclaredFieldValue(
            playerInfoRemovePacket, "b", arrayListOf(
                PLAYER_INFO_DATA_CONSTRUCTOR.newInstance(
                    playerInfoRemovePacket,
                    MinecraftReflection.getGameProfile(player),
                    MinecraftReflection.getPing(player),
                    gameMode,
                    handle.javaClass.getField("listName").get(handle)
                )
            )
        )

        val playerInfoRespawnPacket = MinecraftProtocol.newPacket("PacketPlayOutRespawn")
        Reflection.setDeclaredFieldValue(playerInfoRespawnPacket, "a", player.world.environment.id)
        Reflection.setDeclaredFieldValue(
            playerInfoRespawnPacket, "b", Reflection.getEnum(
                enumDifficulty, player.world.difficulty.name
            )!!
        )

        Reflection.setDeclaredFieldValue(playerInfoRespawnPacket, "c", gameMode!!)
        Reflection.setDeclaredFieldValue(
            playerInfoRespawnPacket,
            "d",
            worldType.getField(player.world.worldType.name).get(null)!!
        )

        MinecraftProtocol.send(player, playerInfoRemovePacket)
        MinecraftProtocol.send(player, playerInfoAddPacket)
        MinecraftProtocol.send(player, playerInfoRespawnPacket)

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

        BukkitUtil.updatePlayerList {
            it.remove(handle)
            it.add(MinecraftReflection.getHandle(player))
        }

        PlayerHandler.findPlayer(player).ifPresent {
            // as they SHOULD already be authenticated
            // if they are executing disguise commands.
            it.authenticateInternal()
        }

        QuickAccess.reloadPlayer(player.uniqueId, false)
    }

    fun fetchDisguiseInfo(name: String, uuid: UUID): DisguiseInfo?
    {
        return try
        {
            val url = URL(
                "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString()
                    .replace("-", "") + "?unsigned=false"
            )

            val json = JsonParser().parse(InputStreamReader(url.openStream())).asJsonObject.get("properties")
                .asJsonArray.get(0).asJsonObject

            val skin: String = json.get("value").asString
            val signature: String = json.get("signature").asString

            DisguiseInfo(uuid, name, skin, signature)
        } catch (exception: Exception)
        {
            null
        }
    }
}
