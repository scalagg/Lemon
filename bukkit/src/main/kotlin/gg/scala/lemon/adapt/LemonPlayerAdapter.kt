package gg.scala.lemon.adapt

import com.google.gson.*
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.serializers.Serializers
import java.lang.reflect.Type
import java.util.*

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
object LemonPlayerAdapter : JsonDeserializer<LemonPlayer>, JsonSerializer<LemonPlayer> {

    @Throws(JsonParseException::class)
    override fun deserialize(src: JsonElement?, type: Type, context: JsonDeserializationContext): LemonPlayer {
        val jsonObject = src as JsonObject
        val lemonPlayer = LemonPlayer(
            UUID.fromString(jsonObject.get("uniqueId").asString),
            jsonObject.get("name").asString,
            null
        )

        lemonPlayer.previousIpAddress = jsonObject.get("ipAddress").asString

        lemonPlayer.ignoring = Serializers.gson.fromJson(jsonObject.get("ignoring"), LemonConstants.UUID_ARRAY_LIST_TYPE)

        lemonPlayer.metadata = Serializers.gson.fromJson(
            jsonObject.get("metadata"),
            LemonConstants.STRING_METADATA_MAP_TYPE
        )

        lemonPlayer.notes = Serializers.gson.fromJson(jsonObject.get("notes"), LemonConstants.NOTE_ARRAY_LIST_TYPE)
        lemonPlayer.pastIpAddresses = Serializers.gson.fromJson(jsonObject.get("pastIpAddresses"), LemonConstants.STRING_LONG_MUTABLEMAP_TYPE)
        lemonPlayer.pastLogins = Serializers.gson.fromJson(jsonObject.get("pastLogins"), LemonConstants.STRING_LONG_MUTABLEMAP_TYPE)

        return lemonPlayer
    }

    override fun serialize(src: LemonPlayer, type: Type, context: JsonSerializationContext): JsonElement {
        val jsonObject = JsonObject()

        jsonObject.add("name", JsonPrimitive(src.name))
        jsonObject.add("uniqueId", JsonPrimitive(src.uniqueId.toString()))
        jsonObject.add("ipAddress", JsonPrimitive(src.ipAddress))

        jsonObject.add("ignoring", Serializers.gson.toJsonTree(src.ignoring))
        jsonObject.add("metadata", Serializers.gson.toJsonTree(src.metadata))
        jsonObject.add("notes", Serializers.gson.toJsonTree(src.notes))
        jsonObject.add("pastIpAddresses", Serializers.gson.toJsonTree(src.pastIpAddresses))
        jsonObject.add("pastLogins", Serializers.gson.toJsonTree(src.pastLogins))

        return jsonObject
    }

}