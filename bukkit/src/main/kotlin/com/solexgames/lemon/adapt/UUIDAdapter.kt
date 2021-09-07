package com.solexgames.lemon.adapt

import com.google.gson.*
import java.lang.reflect.Type
import java.util.*

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
class UUIDAdapter : JsonDeserializer<UUID>, JsonSerializer<UUID> {

    @Throws(JsonParseException::class)
    override fun deserialize(src: JsonElement?, type: Type, context: JsonDeserializationContext): UUID? {
        return if (src == null || src.asString == null || src.asString.equals("null")) {
            null
        } else {
            UUID.fromString(src.asString)
        }
    }

    override fun serialize(src: UUID, type: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }

}
