package gg.scala.common.metadata

data class NetworkProperties(
    var forbiddenCommands: MutableSet<String> = mutableSetOf("plugins", "pl"),
    var allowedCommandsWhenBanned: MutableSet<String> = mutableSetOf("register", "discord", "website"),
    var rankPrefixInNametags: Boolean = true,
    var guildTagsInNametags: Boolean = true,
    var tablistSortingEnabled: Boolean = true,
)
