package gg.scala.lemon.filter.ml

import gg.scala.commons.acf.annotation.CommandAlias
import gg.scala.commons.acf.annotation.CommandPermission
import gg.scala.commons.acf.annotation.Default
import gg.scala.commons.annotations.commands.AutoRegister
import gg.scala.commons.command.ScalaCommand
import gg.scala.commons.issuer.ScalaPlayer
import net.evilblock.cubed.util.CC
import org.bukkit.command.ConsoleCommandSender

/**
 * @author GrowlyX
 * @since 8/21/2024
 */
@AutoRegister
@CommandAlias("chatml-genai-prompt")
@CommandPermission("lemon.command.genai.prompt")
object ChatMLGenAIPromptCommand : ScalaCommand()
{
    @Default
    fun default(player: ConsoleCommandSender, newPrompt: String)
    {
        val cachedConfigModel = ChatMLDataSync.cached()
        cachedConfigModel.generativeAIPrompt = newPrompt
        ChatMLDataSync.sync(cachedConfigModel)

        player.sendMessage("${CC.GREEN}Prompt set to:")
        player.sendMessage("${CC.WHITE}$newPrompt")
    }
}
