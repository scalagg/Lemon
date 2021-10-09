package gg.scala.lemon.util

import gg.scala.lemon.Lemon
import gg.scala.lemon.util.validate.LemonWebData
import net.evilblock.cubed.serializers.Serializers
import java.net.URL
import java.util.*

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
object LemonWebUtil {

    /**
     * Fetches server design information from our API.
     *
     * [id] - The server-specific identifier
     */
    @JvmStatic
    fun fetchServerData(id: String): LemonWebData? {
        return try {
            Scanner(
                URL(
                    "${
                        if (Lemon.instance.settings.serverPasswordHttps) "https" else "http"
                    }://${
                        Lemon.instance.settings.serverPasswordSupplier
                    }/routing/minecraft/$id"
                ).openStream()
            ).useDelimiter("\\A").use { scanner ->
                Serializers.gson.fromJson(
                    scanner.next(), LemonWebData::class.java
                )
            }
        } catch (exception: Exception) {
            exception.printStackTrace(); null
        }
    }
}
