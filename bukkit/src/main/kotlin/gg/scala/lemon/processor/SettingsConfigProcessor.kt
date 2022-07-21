package gg.scala.lemon.processor

import xyz.mkotb.configapi.comment.Comment

class SettingsConfigProcessor
{
    @Comment("What should the server id for this instance be?")
    val id = "server-1"

    @Comment("What should the server group for this instance be?")
    val group = "hub"

    @Comment("What datacenter is this server running on?")
    val datacenter = "na-east-1"

    @Comment("Do we enable consul integration?")
    val consulEnabled = true

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
