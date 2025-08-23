package gg.scala.lemon.proxy

class ProxyPresenceSettings(
    var playerCap: Int = 1000,
    var normalPresence: String = "Hey",
    var whitelistedPresence: String = "cocoaV Lol",
    var whitelistedNotification: String = """
        <red>We're currently whitelisted.
        
        <red>Additional information may be found in:
        <red><bold>discord.gg/asdf
    """.trimIndent(),
    var shouldCenterPresence: Boolean = false
)
