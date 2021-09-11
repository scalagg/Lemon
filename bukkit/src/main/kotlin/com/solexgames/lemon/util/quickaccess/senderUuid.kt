package com.solexgames.lemon.util.quickaccess

import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import java.util.*

/**
 * @author GrowlyX
 * @since 9/10/2021
 */
fun senderUuid(sender: CommandSender): UUID? {
    return if (sender is ConsoleCommandSender) null else (sender as Player).uniqueId
}
