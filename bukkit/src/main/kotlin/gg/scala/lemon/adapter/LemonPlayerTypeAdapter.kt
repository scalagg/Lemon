package gg.scala.lemon.adapter

import com.google.gson.*
import gg.scala.lemon.Lemon
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.player.LemonPlayer
import net.evilblock.cubed.serializers.Serializers
import java.lang.reflect.Type
import java.util.*
import java.util.logging.Level

/**
 * Purely to prevent issues with
 * deserialization of transient fields.
 *
 * @author GrowlyX
 * @since 4/26/2022
 */
object LemonPlayerTypeAdapter : JsonSerializer<LemonPlayer>, JsonDeserializer<LemonPlayer>
{
    override fun serialize(
        player: LemonPlayer,
        type: Type,
        context: JsonSerializationContext
    ): JsonElement
    {
        val jsonObject = JsonObject()

        jsonObject.add(
            "name",
            JsonPrimitive(player.name)
        )

        jsonObject.add(
            "uniqueId",
            JsonPrimitive(player.uniqueId.toString())
        )

        jsonObject.add(
            "timestamp",
            JsonPrimitive(player.timestamp.toString())
        )

        if (player.ipAddress != null)
        {
            jsonObject.add(
                "ipAddress",
                JsonPrimitive(player.ipAddress)
            )
        }

        jsonObject.add(
            "ignoring",
            Serializers.gson.toJsonTree(player.ignoring)
        )
        jsonObject.add(
            "metadata",
            Serializers.gson.toJsonTree(player.metadata)
        )
        jsonObject.add(
            "pastIpAddresses",
            Serializers.gson.toJsonTree(player.pastIpAddresses)
        )
        jsonObject.add(
            "pastLogins",
            Serializers.gson.toJsonTree(player.pastLogins)
        )
        jsonObject.add(
            "specific-permissions",
            Serializers.gson.toJsonTree(player.assignedPermissions)
        )

        return jsonObject
    }

    override fun deserialize(
        element: JsonElement,
        type: Type,
        context: JsonDeserializationContext
    ): LemonPlayer?
    {
        return kotlin.runCatching {
            val jsonObject =
                element as JsonObject

            val player = LemonPlayer(
                UUID.fromString(
                    jsonObject.get("uniqueId").asString
                )
            )

            val accountUsername = jsonObject
                .get("name").asString

            if (player.name != accountUsername)
            {
                player.nameChangeDetected = true
                println("name change detected: $accountUsername -> ${player.name}")
            }

            player.timestamp =
                jsonObject
                    .get("timestamp")
                    .asString.toLong()

            val previous = jsonObject
                .get("ipAddress")
                ?.asString

            if (previous != null)
            {
                player.previousIpAddress = previous
            }

            player.ignoring =
                Serializers.gson.fromJson(
                    jsonObject.get("ignoring"),
                    LemonConstants.UUID_MUTABLE_LIST
                )

            player.metadata =
                Serializers.gson.fromJson(
                    jsonObject.get("metadata"),
                    LemonConstants.STRING_METADATA_MAP_TYPE
                )

            val pastIpAddresses = jsonObject
                .get("pastIpAddresses")

            player.pastIpAddresses = kotlin
                .runCatching {
                    Serializers.gson
                        .fromJson(
                            pastIpAddresses,
                            LemonConstants.STRING_MUTABLE_LIST
                        ) as MutableList<String>
                }.getOrNull()
                ?: (Serializers.gson
                    .fromJson(
                        pastIpAddresses,
                        LemonConstants.STRING_LONG_MUTABLE_MAP_TYPE
                    ) as MutableMap<String, Long>).keys.toMutableList()

            player.pastLogins =
                Serializers.gson.fromJson(
                    jsonObject.get("pastLogins"),
                    LemonConstants.STRING_LONG_MUTABLE_MAP_TYPE
                )

            player.assignedPermissions = Serializers.gson
                .fromJson(
                    jsonObject.get("specific-permissions"),
                    LemonConstants.STRING_MUTABLE_LIST
                )

            return@runCatching player
        }.onFailure {
            Lemon.instance.logger.log(
                Level.WARNING, "Exception thrown during LemonPlayer deserialization", it
            )
        }.getOrNull()
    }
}
