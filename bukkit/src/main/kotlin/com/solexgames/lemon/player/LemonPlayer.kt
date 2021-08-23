package com.solexgames.lemon.player

import java.net.InetAddress
import java.util.*

class LemonPlayer(uuid: UUID, name: String, address: InetAddress?) {

    var uuid = uuid
    var name = name
    var ipAddress = if (address != null) { address.hostAddress } else { "" }
//    var encryptedIpAdress: String

    fun checkGrants() {
        // check for any grants that are expired but not yet removed
        //
        // apply default grant if no active grant
    }

}