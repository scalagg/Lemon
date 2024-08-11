package gg.scala.lemon.filter

import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.filter.impl.RepetitiveMessageFilter
import gg.scala.lemon.filter.ml.ChatMLMessage
import gg.scala.lemon.filter.ml.ChatMLService
import gg.scala.lemon.filter.ml.IncubatorChatML
import gg.scala.lemon.filter.phrase.MessagePhraseFilter
import gg.scala.lemon.filter.phrase.impl.MinequestInvalidCharFilter
import gg.scala.lemon.filter.phrase.impl.RegexPhraseFilter
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.PunishmentHandler.handlePunishmentForTargetPlayerGlobally
import gg.scala.lemon.minequest
import gg.scala.lemon.player.punishment.category.PunishmentCategory
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.QuickAccess
import gg.scala.lemon.util.QuickAccess.isSilent
import gg.scala.lemon.util.QuickAccess.parseReason
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID

/**
 * @author GrowlyX
 * @since 12/29/2021
 */
@Service
object ChatMessageFilterHandler
{
    val phraseFilters = mutableListOf<MessagePhraseFilter>()
    private val messageFilters = mutableListOf<ChatMessageFilter>()

    @Configure
    fun configure()
    {
        ChatMLService.configure()

        phraseFilters.add(RegexPhraseFilter)
//        messageFilters.add(RepetitiveMessageFilter)

        if (minequest())
        {
            phraseFilters.add(MinequestInvalidCharFilter)
        }

        continuedConfigure()
    }

    private fun continuedConfigure()
    {
        phraseFilters.forEach {
            it.loadResources()
        }

        messageFilters.forEach {
            it.loadResources()
        }
    }

    fun handleMessageFilter(
        player: Player, message: String,
        reportToStaff: Boolean = true,
        target: UUID? = null
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
            report.add(CC.GRAY + "violated ${CC.WHITE}$filteredPhrasesCount${CC.GRAY} filter${
                if (filteredPhrasesCount == 1) "" else "s"
            }.")
        }

        val shouldAllowMessage =
            filteredMessages == null && filteredPhrasesCount == 0

        if (!shouldAllowMessage)
        {
            val fancyMessage = FancyMessage()
            fancyMessage.withMessage("${CC.D_RED}[Filter] ")

            if (target != null)
            {
                fancyMessage.withMessage(
                    "${CC.GRAY}(${QuickAccess.coloredName(player)}${CC.GRAY} -> ${QuickAccess.coloredName(target) ?: CubedCacheUtil.fetchName(target)}${CC.GRAY})"
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

            if (reportToStaff)
            {
                Bukkit.getOnlinePlayers()
                    .mapNotNull { PlayerHandler.findPlayer(it).orElse(null) }
                    .filter { it.hasPermission("scstaff.staff-member") }
                    .filter { !it.hasMetadata("filter-disabled") }
                    .forEach {
                        fancyMessage.sendToPlayer(it.bukkitPlayer!!)
                    }
            }
        }

        if (!player.hasPermission("lemon.filter.machinelearning.chat-bypass"))
        {
            ChatMLService.submit(ChatMLMessage(message) {
                DataStoreObjectControllerCache
                    .findNotNull<IncubatorChatML>()
                    .save(
                        IncubatorChatML(UUID.randomUUID(), message, it),
                        DataStoreStorageType.MONGO
                    )
                    .thenAccept { _ ->
                        ChatMLService.webhookClient?.send(
                            """
                                **ChatML Prediction:**
                                $message
                                
                                *(prediction: $it)*
                            """.trimIndent()
                        )
                    }
            })
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
