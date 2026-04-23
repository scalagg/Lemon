package gg.scala.lemon.serialization

import com.google.gson.*
import java.lang.reflect.Type

/**
 * @author GrowlyX
 * @since 8/17/2024
 */
object BsonTimestampSerializer : JsonDeserializer<BsonTimestamp>, JsonSerializer<BsonTimestamp>
{
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): BsonTimestamp
    {
        return BsonTimestamp(
            json.asJsonObject
                .get("\$numberLong")
                .asLong
        )
    }

    override fun serialize(
        src: BsonTimestamp,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.value)
    }
}
