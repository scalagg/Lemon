package gg.scala.lemon.processor

import gg.scala.commons.agnostic.sync.ServerSync
import xyz.mkotb.configapi.comment.Comment

class SettingsConfigProcessor
{
    val id: String
        get() = ServerSync.getLocalGameServer().id

    val group: String
        get() = ServerSync.getLocalGameServer().groups.first()

    @Comment("Dummy server?")
    val dummyServer = false

    @Comment("What's the password to your network details?")
    val serverPassword = "server_password"
    val serverPasswordSupplier = "127.0.0.1"
    val serverPasswordHttps = false

    val noNotifyRedirection = false

    @Comment("Shall console be able to view chat messages?")
    val consoleChat = false

    val defaultChatGsd = false
    val defaultChatGsdGroupId = ""
    val tabWeight = "§9§9§9§9"

    val tablistSortingEnabled = true

    val blacklistedCommands = listOf(
        "ver", "icanhasbukkit"
    )

    val blacklistedPhraseRegex = listOf(
        "(n|i){1,32}((g{2,32}|q){1,32}|[gq]{2,32})[e3ra]{1,32}",
        "\\\$\\{*\\}"
    )
}
