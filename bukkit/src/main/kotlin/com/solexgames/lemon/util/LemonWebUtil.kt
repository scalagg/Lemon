package com.solexgames.lemon.util

import com.solexgames.lemon.util.validate.LemonWebData
import net.evilblock.cubed.serializers.Serializers
import java.net.URL
import java.util.*

/**
 * @author GrowlyX
 * @since 8/26/2021
 */
object LemonWebUtil {

    @JvmStatic
    fun fetchServerData(id: String): LemonWebData? {
        return try {
            Scanner(
                URL(
                    "https://api.solexgames.com/fetch?id=$id"
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
