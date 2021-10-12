package gg.scala.lemon.server

data class ServerInstance(
    val serverId: String,
    val serverGroup: String
) {
    val metaData = mutableMapOf<String, String>()

    var maxPlayers: Int = 0
    var onlinePlayers: Int = 0

    var ticksPerSecond: Double = 0.0

    var whitelisted: Boolean = false
    var onlineMode: Boolean = false

    var version: String = "Unknown"

    var lastHeartbeat: Long = 0
}
