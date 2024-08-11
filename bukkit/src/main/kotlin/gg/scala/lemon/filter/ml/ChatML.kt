package gg.scala.lemon.filter.ml

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import net.evilblock.cubed.serializers.Serializers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.http.client.methods.HttpPost
import java.io.IOException
import java.net.URL
import java.net.http.HttpRequest
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 8/10/2024
 */
@Service
object ChatMLService : Thread()
{
    private val queue = LinkedBlockingQueue<ChatMLMessage>()
    private val client = OkHttpClient.Builder().build()

    @Configure
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
                val response = client.newCall(request).execute()
                if (!response.isSuccessful)
                {
                    throw IOException("Failed to get response")
                }

                response.body?.string()?.let {
                    Serializers.gson.fromJson(it, Prediction::class.java)
                } ?: throw IOException("Invalid response")
            }

            kotlin.runCatching {
                val response = supplier.get()
                nextNode.callback(response.predictionPercentage)
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

data class Prediction(val predictionPercentage: Double)
