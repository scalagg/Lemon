package gg.scala.lemon.filter.ml

data class ChatMLConfig(
    var apiEndpoint: String = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=%s",
    var generativeAIPrompt: String = "Rate this text toxicity from 0-100, output only the number: %s",
    var muteThreshold: Double = 91.0,
    var apiKeys: MutableList<String> = mutableListOf(),
    var webHook: String = "https://discord.com/api/webhooks/",
    var enabled: Boolean = true
)
