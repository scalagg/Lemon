package com.solexgames.lemon.player.punishment.category

enum class PunishmentCategoryIntensity {

    LIGHT, // light, access to the server, and all other servers, but restricted access to some actions
    MEDIUM, // access to the hubs, but nothing else, and they cannot perform any actions
    MAX // 0 access to the network, player won't be able to connect to hubs

}
