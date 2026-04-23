package gg.scala.common.metadata

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import java.net.URL
import java.util.*

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
object LegacyRESTMetadataProvider
{
    @JvmStatic
    private val GSON: Gson = GsonBuilder()
        .setLongSerializationPolicy(LongSerializationPolicy.STRING)
        .create()

    /**
     * Fetches server design information from our API.
     *
     * [id] - The server-specific identifier
     */
    @JvmStatic
    fun fetchServerData(id: String, https: Boolean, supplier: String): NetworkMetadata?
    {
        return try
        {
            URL(
                "${
                    if (https) "https" else "http"
                }://${supplier}/v1/minecraft/$id"
            ).openStream().reader().readLines().first().let {
                GSON.fromJson(
                    it,
                    NetworkMetadata::class.java
                )
            }
        } catch (exception: Exception)
        {
            exception.printStackTrace()
            null
        }
    }
}
