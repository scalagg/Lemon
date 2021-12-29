package gg.scala.lemon.filter

import gg.scala.lemon.filter.impl.RepetitiveMessageFilter
import gg.scala.lemon.filter.phrase.MessagePhraseFilter
import gg.scala.lemon.filter.phrase.impl.RegexPhraseFilter
import gg.scala.lemon.handler.FilterHandler
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
object ChatMessageFilterHandler
{
    private val phraseFilters = mutableListOf<MessagePhraseFilter>()
    private val messageFilters = mutableListOf<ChatMessageFilter>()

    fun initialLoad()
    {
        phraseFilters.add(RegexPhraseFilter)
        messageFilters.add(RepetitiveMessageFilter)

        continuedInitialLoad()
    }

    private fun continuedInitialLoad()
    {
        phraseFilters.forEach {
            it.loadResources()
        }

        messageFilters.forEach {
            it.loadResources()
        }
    }

    fun handleMessageFilter(
        player: Player, message: String, target: Player? = null
    ): Boolean
    {
        val report = mutableListOf<String>()

        val filteredMessages = messageFilters
            .firstOrNull {
                it.isFiltered(player, message)
            }

        filteredMessages?.let {
            report.add(
                CC.GRAY + it.formDescription(player)
            )
        }

        val phrases = message.split(' ')

        val filteredPhrases = phrases
            .map { phrase ->
                val sanitized = phrase.sanitized()

                phraseFilters.filter {
                    it.isFiltered(player, sanitized)
                }
            }

        val containsFiltered = filteredPhrases
            .filter { it.isNotEmpty() }

        val filteredPhrasesCount = containsFiltered
            .map { it.size }.count()

        if (filteredPhrasesCount > 0)
        {
            // We're checking for pre-existing
            // reports (for display purposes)
            if (report.isNotEmpty())
            {
                report.add("")
            }

            report.add(CC.GRAY + "This message contains ${CC.WHITE}${containsFiltered.size}${CC.GRAY} phrase${
                if (containsFiltered.size == 1) "" else "s"
            } which")
            report.add(CC.GRAY + "flagged ${CC.WHITE}$filteredPhrasesCount${CC.GRAY} filter${
                if (filteredPhrasesCount == 1) "" else "s"
            } altogether.")
        }

        val shouldAllowMessage =
            filteredMessages == null && filteredPhrasesCount == 0

        if (!shouldAllowMessage)
        {
            val fancyMessage = FancyMessage()
            fancyMessage.withMessage("${CC.RED}[Filter] ")

            if (target != null)
            {
                fancyMessage.withMessage(
                    "${CC.GRAY}(${player.name} -> ${target.name})"
                )
            } else
            {
                fancyMessage.withMessage(
                    "${QuickAccess.coloredName(player)}"
                )
            }

            fancyMessage.withMessage(
                "${CC.WHITE}: $message"
            )

            fancyMessage.andHoverOf(
                "${CC.RED}This message was blocked from being sent.",
                *report.toTypedArray()
            )

            Bukkit.getOnlinePlayers()
                .mapNotNull { PlayerHandler.findPlayer(it).orElse(null) }
                .filter { it.hasPermission("lemon.staff")/* && !it.getSetting("filtered-messages-disabled")*/ }
                .forEach {
                    fancyMessage.sendToPlayer(it.bukkitPlayer!!)

                    println("sent to ${it.name}")
                }
        }

        return !shouldAllowMessage
    }

    private fun String.sanitized(): String
    {
        return lowercase()
            .replace(oldValue = "@", newValue = "a")
            .replace(oldValue = "3", newValue = "e")
            .replace(oldValue = "0", newValue = "o")
            .replace(oldValue = "4", newValue = "a")
            .replace(oldValue = "1", newValue = "i")
            .replace(oldValue = "5", newValue = "s")
            .replace(oldValue = "\"", newValue = "")
    }
}
