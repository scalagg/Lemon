package gg.scala.lemon.serialization

import com.google.gson.*
import java.lang.reflect.Type

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
