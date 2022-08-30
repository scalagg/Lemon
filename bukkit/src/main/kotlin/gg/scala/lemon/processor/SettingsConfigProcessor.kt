package gg.scala.lemon.processor

import gg.scala.commons.agnostic.sync.ServerSync
import xyz.mkotb.configapi.comment.Comment

class SettingsConfigProcessor
{
    val id: String
        get() = ServerSync.getLocalGameServer().id

    val group: String
        get() = ServerSync.getLocalGameServer().groups.first()

    @Comment("Do we enable consul integration?")
    val consulEnabled = true

    @Comment("Dummy server?")
    val dummyServer = false

    @Comment("What is the server address?")
    val serverAddress = "127.0.0.1"

    @Comment("What are the consul connection details?")
    val consulAddress = "127.0.0.1"
    val consulPort = 8500

    @Comment("What's the password to your network details?")
    val serverPassword = "server_password"
    val serverPasswordSupplier = "127.0.0.1"
    val serverPasswordHttps = false

    @Comment("Should we auto-ban players if they disconnect whilst frozen?")
    val frozenAutoBan = false

    @Comment("Shall console be able to view chat messages?")
    val consoleChat = false

    @Comment("Should we log important data to files?")
    val logDataToFile = false

    @Comment("Should we enable our disguise system on this server?")
    val disguiseEnabled = false

    @Comment("Should we enable the /color command?")
    val playerColorsEnabled = true

    @Comment("Should we enable rank-based tablist sorting?")
    val tablistSortingEnabled = true

    val blacklistedCommands = listOf(
        "ver", "icanhasbukkit"
    )

    val blacklistedPhraseRegex = listOf(
        "(n|i){1,32}((g{2,32}|q){1,32}|[gq]{2,32})[e3ra]{1,32}",
        "\\\$\\{*\\}"
    )
}
