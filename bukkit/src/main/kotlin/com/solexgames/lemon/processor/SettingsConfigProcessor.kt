package com.solexgames.lemon.processor

import xyz.mkotb.configapi.comment.Comment

object SettingsConfigProcessor {

    @Comment("What should the server id for this instance be?")
    val id: String = "server-1"

    @Comment("What should the server group for this instance be?")
    val group: String = "hub"

    @Comment("What's the password to your network details?")
    val serverPassword: String = "sg-lool"

}
