package com.solexgames.lemon.server

import com.solexgames.lemon.server.enum.ServerStatus
import com.solexgames.lemon.server.enum.ServerType

class ServerInstance(serverName: String, serverType: ServerType) {

    var onlinePlayers: List<String> = listOf()
    var whitelist: List<String> = listOf()

    var serverName = serverName
    var ticksPerSecond: String = ""
    var ticksPerSecondSimple: String = ""

    var serverType = serverType
    var serverStatus: ServerStatus = ServerStatus.BOOTING

    var maxPlayers: Int = 20

    var whitelisted: Boolean = false
    var lastUpdate: Long = 0

    fun updateStatus(online: Boolean, whitelisted: Boolean) {
        serverStatus = if (whitelisted && online) {
            ServerStatus.WHITELISTED
        } else if (online) {
            ServerStatus.ONLINE
        } else {
            ServerStatus.OFFLINE
        }
    }
}