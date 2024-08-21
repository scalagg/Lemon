package gg.scala.lemon.filter.ml

/**
 * @author GrowlyX
 * @since 8/21/2024
 */
data class ChatMLConfig(
    var apiEndpoint: String = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s",
    var generativeAIPrompt: String = "Rate this text toxicity from 0-100, output only the number: %s",
    var muteThreshold: Double = 91.0,
    var enabled: Boolean = true
)
