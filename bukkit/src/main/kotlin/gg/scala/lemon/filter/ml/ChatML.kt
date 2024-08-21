package gg.scala.lemon.filter.ml

import club.minnced.discord.webhook.WebhookClientBuilder
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import net.evilblock.cubed.serializers.Serializers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.lang.mutable.Mutable
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 8/10/2024
 */
object ChatMLService : Thread()
{
    val queue = LinkedBlockingQueue<ChatMLMessage>()
    val client = OkHttpClient.Builder().build()
    // val client = HttpClient.newBuilder().build()
    val url = "https://generativelanguage.googleapis.com/v1beta"
    val webhookURL = with(File("/opt/data/discord.webhook")) {
        if (exists())
        {
            return@with readText()
        }

        return@with null
    }
    val keys = with(File("/opt/data/gemini.tokens")) {
        if (exists()) {
            return@with readLines()
        }

        return@with null
    }

    val webhookClient = with(webhookURL) {
        if (this != null)
        {
            return@with WebhookClientBuilder(this)
                .setWait(true)
                .build()
        }

        return@with null
    }

    val testedKeys = mutableMapOf<String, Long>()

    fun configure()
    {
        start()
    }

    fun submit(message: ChatMLMessage)
    {
        queue += message
    }

    override fun run()
    {
        while (true)
        {
            val nextNode = queue.poll(1L, TimeUnit.SECONDS)
                ?: continue

            testedKeys.values.removeIf { System.currentTimeMillis() - 60000 > it }

            val key = getKey(testedKeys) ?: return

            val request = Request.Builder()
                .url("$url/models/gemini-1.5-flash:generateContent?key=$key")
                .method(
                    "POST",
                    Serializers.gson
                        .toJson("{\"contents\":[{\"parts\":[{\"text\":'$nextNode'}]}],\"safetySettings\":[{\"category\":\"HARM_CATEGORY_SEXUALLY_EXPLICIT\",\"threshold\":\"BLOCK_NONE\"},{\"category\":\"HARM_CATEGORY_DANGEROUS_CONTENT\",\"threshold\":\"BLOCK_NONE\"},{\"category\":\"HARM_CATEGORY_HARASSMENT\",\"threshold\":\"BLOCK_NONE\"}]}")
                        .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                )
                .build()

            val supplier = mlCircuitBreaker.decorateSupplier {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful)
                    {
                        testedKeys[key] = System.currentTimeMillis()
                        throw IOException("Failed to get response")
                    }

                    response.body?.string()?.let {
                        val obj = JSONObject(it)

                        var str = obj.getJSONArray("candidates").getJSONObject(0).getJSONArray("parts").getJSONObject(0).getString("text")

                        try {
                            str = str.replace(" ", "").replace("\n", "")
                            Prediction(str.toInt() + 0.0)
                        } catch (e: Exception) {
                            throw IOException("Invalid response: $str")
                        }
                    } ?: throw IOException("Invalid response")
                }
            }

            kotlin.runCatching {
                val response = supplier.get()
                nextNode.callback(response.prediction)
            }
        }
    }

    fun getKey(tested: MutableMap<String, Long>) : String? {
        if (keys == null) return null
        return keys.firstOrNull { !tested.contains(it) }
    }
}

val mlCircuitBreakerConfig = CircuitBreakerConfig.custom()
    .failureRateThreshold(50F)
    .waitDurationInOpenState(Duration.ofSeconds(30))
    .permittedNumberOfCallsInHalfOpenState(5)
    .slidingWindowSize(10)
    .build()

val mlCircuitBreakerRegistry = CircuitBreakerRegistry.of(mlCircuitBreakerConfig)
val mlCircuitBreaker = mlCircuitBreakerRegistry.circuitBreaker("ml")

data class ChatMLMessage(val message: String, val callback: (Double) -> Unit)

data class Prediction(val prediction: Double)