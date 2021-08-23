package com.solexgames.lemon.player

import com.solexgames.lemon.player.grant.Grant
import java.net.InetAddress
import java.util.*

class LemonPlayer(uuid: UUID, name: String, address: InetAddress?) {

//    var punishments: List<Punishment>
    var grants: List<Grant> = listOf()
    var notes: List<Grant> = listOf()
    var prefixes: List<String> = listOf()
    var ignoring: List<String> = listOf()
    var permissions: List<String> = listOf()
    var bungeePermissions: List<String> = listOf()

    var uuid = uuid
    var name = name
    var ipAddress = if (address != null) { address.hostAddress } else { "" }
//    var encryptedIpAdress: String

    var canSeeStaffMessages = true
    var canSeeGlobalChat = true
    var canReceiveDms = true
    var canReceiveDmsSounds = true
    var canSeeFiltered = true
    var canSeeTips = true
    var canReport = true
    var canRequest = true

    var hasVoted = false
    var hasActiveWarning = false
    var hasSetup2FA = false

    var isVanished = false
    var isStaffMode = false
    var isFrozen = false
    var isSynced = false
    var isLoaded = false
    var isSocialSpy = false
    var isAutoVanish = false
    var isAutoModMode = false
    var isDisguised = false
    var hidingStaff = false

    var requiredToAuth = false
    var authBypassed = false

    fun checkGrants() {
        // check for any grants that are expired but not yet removed
        //
        // apply default grant if no active grant
    }

}