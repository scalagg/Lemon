package gg.scala.lemon.serialization

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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
