package gg.scala.lemon.processor

import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.lemon.metadata.NetworkMetadataDataSync
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

    val tablistSortingEnabled: Boolean
        get() = NetworkMetadataDataSync.cached().properties()
            .tablistSortingEnabled

    val prefixInNametags: Boolean
        get() = NetworkMetadataDataSync.cached().properties()
            .rankPrefixInNametags

    val blacklistedCommands: List<String>
        get() = NetworkMetadataDataSync.cached().properties()
            .forbiddenCommands
            .toList()
}
