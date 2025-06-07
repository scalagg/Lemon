package gg.scala.lemon.proxy

import java.util.UUID

/**
 * @author GrowlyX
 * @since 12/15/2021
 */
class ProxySettings(
    var showPlayerCountWhileWhitelisted: Boolean = false,
    var autoRegisterFromServerSyncDiscovery: Boolean = true,
    var ensureTokenWhitelisted: Boolean = false,
    var tokenWhitelistHighestWeightBeforeNotEffective: Int = 99,
    var serverBrand: String = "1.8.9-1.19.2",
    var middleware: String? = null,
    var listeningGroups: MutableList<String> = mutableListOf("practice", "hub"),
    var listeningExemptions: MutableList<String> = mutableListOf("build"),
    var networking: NetworkingSettings = NetworkingSettings(),
    var preferHubServerRedirects: MutableMap<String, String> = mutableMapOf(
        "mipgame" to "miplobby"
    ),
    var totpIssuer: String = "Scala%20Studios",
    var countdown: ProxyCountdown = ProxyCountdown(),
    var maintenance: MaintenanceSettings = MaintenanceSettings(),
    var allowedVersions: MutableList<Int> = mutableListOf(),
    var allowedVersionRanges: MutableList<Pair<Int, Int>> = mutableListOf(),
    var shouldRestrictAllowedVersions: Boolean = true,
    var presence: ProxyPresenceSettings = ProxyPresenceSettings(),
)
{
    data class MaintenanceSettings(
        var enabled: Boolean = false,
        var rank: UUID? = null
    )

    data class NetworkingSettings(
        val handleNetworkingChecks: Boolean = false,
        val apiKey: String = ""
    )
}
