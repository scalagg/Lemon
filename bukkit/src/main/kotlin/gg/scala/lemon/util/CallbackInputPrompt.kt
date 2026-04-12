package gg.scala.lemon.util

import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.entity.Player

class CallbackInputPrompt(val prompt: String, private val callback: (String) -> Unit)
{
    fun start(player: Player) =
        InputPrompt()
            .withText(prompt)
            .acceptInput { _, input -> callback.invoke(input) }
            .start(player)
}