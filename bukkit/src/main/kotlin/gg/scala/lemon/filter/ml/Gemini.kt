package gg.scala.lemon.filter.ml

/**
 * @author GrowlyX
 * @since 8/21/2024
 */
data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata
)

data class Candidate(
    val content: Content,
    val finishReason: String,
    val index: Int,
    val safetyRatings: List<SafetyRating>
)

data class Content(
    val parts: List<Part>,
    val role: String
)

data class Part(
    val text: String
)

data class SafetyRating(
    val category: String,
    val probability: String
)

data class UsageMetadata(
    val promptTokenCount: Int,
    val candidatesTokenCount: Int,
    val totalTokenCount: Int
)
