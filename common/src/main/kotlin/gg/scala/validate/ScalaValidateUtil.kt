package gg.scala.validate

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import java.net.URL
import java.util.*

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
object ScalaValidateUtil
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
    fun fetchServerData(id: String, https: Boolean, supplier: String): ScalaValidateData?
    {
        return try
        {
            Scanner(
                URL(
                    "${
                        if (https) "https" else "http"
                    }://${
                        supplier
                    }/routing/minecraft/$id"
                ).openStream()
            ).useDelimiter("\\A").use { scanner ->
                GSON.fromJson(
                    scanner.next(), ScalaValidateData::class.java
                )
            }
        } catch (exception: Exception)
        {
            exception.printStackTrace(); null
        }
    }
}
