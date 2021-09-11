package com.solexgames.lemon.util.quickaccess

import com.solexgames.lemon.Lemon
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/4/2021
 */
fun startContinuousPrompt(prompt: StringPrompt, player: Player) {
    Tasks.sync {
        Lemon.instance.conversationFactory
            .withFirstPrompt(prompt)
            .withLocalEcho(false)
            .buildConversation(player)
            .begin()
    }
}
