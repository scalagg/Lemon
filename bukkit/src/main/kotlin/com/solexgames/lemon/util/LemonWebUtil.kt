package com.solexgames.lemon.util

import com.google.gson.JsonParser
import com.solexgames.lemon.LemonConstants
import com.solexgames.lemon.util.validate.LemonWebData
import com.solexgames.lemon.util.validate.LemonWebStatus
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 8/26/2021
 */

object LemonWebUtil {

    @JvmStatic
    val BASE_URL: String = "https://api.solexgames.com/fetch?id="

    @JvmStatic
    fun fetchServerData(id: String): CompletableFuture<LemonWebData> {
        return CompletableFuture.supplyAsync {
            try {
                val url = URL("${BASE_URL}$id")
                val json = JsonParser().parse(InputStreamReader(url.openStream())).asJsonObject

//                return@supplyAsync LemonConstants.GSON.fromJson(json.toString(), LemonWebData::class.java)
                return@supplyAsync LemonWebData(
                    LemonWebStatus.SUCCESS,
                    "DEV",
                    "SolexGames",
                    "GOLD",
                    "YELLOW",
                    "discord.gg/solexgames",
                    "twitter.com/solexgames",
                    "solexgames.com",
                    "store.solexgames.com"
                )
            } catch (ignored: Exception) {
                null
            }
        }
    }
}
