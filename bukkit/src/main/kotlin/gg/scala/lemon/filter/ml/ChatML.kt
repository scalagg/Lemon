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
    val keys = with(File("/opt/data/gemini.tokens")) {
        if (exists())
        {
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

    val testedTokens = mutableMapOf<String, Long>()

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

            testedTokens.values.removeIf { System.currentTimeMillis() - 60000 > it }

            val config = ChatMLDataSync.cached()
            val apiToken = getKey(testedTokens) ?: return
            val prompt = config.generativeAIPrompt.format(nextNode)

            val geminiRequest = GeminiRequest(
                contents = listOf(
                    GeminiRequestContent(parts = listOf(Part(prompt)))
                ),
                safetySettings = listOf(
                    SafetySetting(category = "HARM_CATEGORY_SEXUALLY_EXPLICIT", threshold = "BLOCK_NONE"),
                    SafetySetting(category = "HARM_CATEGORY_DANGEROUS_CONTENT", threshold = "BLOCK_NONE"),
                    SafetySetting(category = "HARM_CATEGORY_HARASSMENT", threshold = "BLOCK_NONE")
                )
            )

            val request = Request.Builder()
                .url(config.apiEndpoint.format(apiToken))
                .method(
                    "POST",
                    Serializers.gson
                        .toJson(geminiRequest)
                        .toRequestBody(
                            "application/json; charset=utf-8".toMediaTypeOrNull()
                        )
                )
                .build()

            val supplier = mlCircuitBreaker.decorateSupplier {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful)
                    {
                        testedTokens[apiToken] = System.currentTimeMillis()
                        throw IOException("Failed to get response")
                    }

                    val geminiResponse = response.body?.string()
                        ?.apply { println(this) }
                        ?.let {
                            Serializers.gson.fromJson(it, GeminiResponse::class.java)
                        }
                        ?: return@use Prediction(0.0)

                    val prediction = extractFirstNumber(geminiResponse)
                    Prediction(prediction?.toDouble() ?: 0.0)
                }
            }

            kotlin.runCatching {
                val response = supplier.get()
                nextNode.callback(response.prediction)
            }
        }
    }

    private fun extractFirstNumber(jsonResponse: GeminiResponse): Int? {
        val regex = "\\d+".toRegex()
        val firstCandidate = jsonResponse.candidates.firstOrNull()
        val firstPartText = firstCandidate?.content?.parts?.firstOrNull()?.text
        return firstPartText?.let {
            regex.find(it)?.value?.toIntOrNull()
        }
    }

    fun getKey(tested: MutableMap<String, Long>): String?
    {
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
