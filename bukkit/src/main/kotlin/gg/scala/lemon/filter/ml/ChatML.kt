package gg.scala.lemon.filter.ml

import club.minnced.discord.webhook.WebhookClientBuilder
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import net.evilblock.cubed.serializers.Serializers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.time.Duration
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
    val webhookURL = with(File("/opt/data/discord.webhook")) {
        if (exists())
        {
            return@with readText()
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

            val request = Request.Builder()
                .url("http://localhost:4000/predict")
                .method(
                    "POST",
                    Serializers.gson
                        .toJson(nextNode)
                        .toRequestBody(
                            "application/json; charset=utf-8".toMediaTypeOrNull()
                        )
                )
                .build()

            val supplier = mlCircuitBreaker.decorateSupplier {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful)
                    {
                        throw IOException("Failed to get response")
                    }

                    response.body?.string()?.let {
                        Serializers.gson.fromJson(it, Prediction::class.java)
                    } ?: throw IOException("Invalid response")
                }
            }

            kotlin.runCatching {
                val response = supplier.get()
                nextNode.callback(response.prediction)
            }
        }
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
