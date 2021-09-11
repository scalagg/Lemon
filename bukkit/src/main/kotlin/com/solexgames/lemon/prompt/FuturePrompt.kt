package com.solexgames.lemon.prompt

import net.evilblock.cubed.util.CC
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt

/**
 * @author GrowlyX
 * @since 9/4/2021
 */
open class FuturePrompt(
    private val onCompletion: (ConversationContext, String) -> Prompt?,
    private val name: String,
) : StringPrompt() {

    override fun getPromptText(conversationContext: ConversationContext?): String {
        return "${CC.SEC}Please enter the ${CC.PRI}${this.name}${CC.SEC}. ${CC.GRAY}(Type stop to exit)"
    }

    override fun acceptInput(conversationContext: ConversationContext, input: String): Prompt? {
        if (input == "stop") {
            conversationContext.forWhom.sendRawMessage(
                "${CC.RED}You've cancelled the ${CC.YELLOW}${this.name}${CC.RED} setup."
            )

            return null
        }

        conversationContext.forWhom.sendRawMessage(
            "${CC.SEC}You've set the ${CC.PRI}${this.name}${CC.SEC} to ${CC.WHITE}$input${CC.SEC}."
        )

        return this.onCompletion.invoke(conversationContext, input)
    }

}
