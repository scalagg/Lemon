package com.solexgames.lemon.server.enum

enum class ServerStatus(statusString: String, fancyStatus: String) {

    BOOTING("Booting", "&6Booting..."),
    ONLINE("Online", "&aOnline"),
    OFFLINE("Offline", "&cOffline"),
    WHITELISTED("Whitelisted", "&eWhitelisted");

}