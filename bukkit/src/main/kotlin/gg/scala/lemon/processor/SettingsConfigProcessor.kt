package gg.scala.lemon.processor

import xyz.mkotb.configapi.comment.Comment

class SettingsConfigProcessor {

    @Comment("What should the server id for this instance be?")
    val id: String = "server-1"

    @Comment("What should the server group for this instance be?")
    val group: String = "hub"

    @Comment("What's the password to your network details?")
    val serverPassword: String = "server_password"

    @Comment("Shall console be able to view chat messages?")
    val consoleChat: Boolean = false

    @Comment("Should we enable our disguise system on this server?")
    val disguiseEnabled: Boolean = true

    val blacklistedCommands = listOf(
        "/ver", "/icanhasbukkit"
    )

    val blacklistedPhraseRegex = listOf(
        "(n|i){1,32}((g{2,32}|q){1,32}|[gq]{2,32})[e3ra]{1,32}"
    )

}
