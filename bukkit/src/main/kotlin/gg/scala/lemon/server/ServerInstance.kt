package gg.scala.lemon.server

import gg.scala.store.storage.storable.IDataStoreObject
import java.util.*

data class ServerInstance(
    val serverId: String,
    val serverGroup: String,
    override val identifier: UUID = UUID.randomUUID()
) : IDataStoreObject {
    var metaData = mutableMapOf<String, String>()

    var maxPlayers: Int = 0
    var onlinePlayers: Int = 0

    var ticksPerSecond: Double = 0.0

    var whitelisted: Boolean = false
    var onlineMode: Boolean = false

    var version: String = "Unknown"

    var lastHeartbeat: Long = 0
}
