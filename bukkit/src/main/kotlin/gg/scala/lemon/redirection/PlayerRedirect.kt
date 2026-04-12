package gg.scala.lemon.redirection

import net.evilblock.cubed.util.bukkit.FancyMessage
import java.util.UUID

interface PlayerRedirect<T>
{
    fun redirect(player: T, server: PlayerRedirectMessage)

    fun retrieve(uniqueId: UUID): T?
    fun retrieve(t: T): UUID

    fun sendMessage(player: T, message: FancyMessage)
}
