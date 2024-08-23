package gg.scala.lemon.serialization

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * A special serialization policy that handles serialization
 * to and from a Bson date when Gson is being used.
 *
 * Useful where a Bson date field is needed for scenarios like Mongo
 * TTL indexes, but Gson's default serialization policies automatically turn it
 * into a string JsonPrimitive data type.
 *
 * We only handle formatting INTO the Bson date format since the Bson
 * [org.bson.json.JsonReader] automatically converts the date back into a
 * Long format for us on deserialization (don't ask me why).
 *
 * @author GrowlyX
 * @since 1/16/2024
 */
object BsonDateTimeSerializer : JsonSerializer<BsonDateTime>, JsonDeserializer<BsonDateTime>
{
    private fun format(dateTime: Long): String = ZonedDateTime
        .ofInstant(
            Instant.ofEpochMilli(dateTime),
            ZoneId.of("Z")
        )
        .format(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
        )

    override fun serialize(
        src: BsonDateTime?, typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement
    {
        if (src == null)
        {
            return JsonNull.INSTANCE
        }

        val jsonObject = JsonObject()
        jsonObject.addProperty(
            "\$date",
            format(src.epochMillis)
        )

        return jsonObject
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?, typeOfT: Type,
        context: JsonDeserializationContext
    ): BsonDateTime
    {
        if (json == null || json !is JsonObject || !json.isJsonObject)
        {
            throw JsonParseException("Invalid date format")
        }

        val internalDateString = json.asJsonObject
            .get("\$date")
            .asString

        return BsonDateTime(
            epochMillis = internalDateString.toLong()
        )
    }
}
