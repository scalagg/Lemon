package gg.scala.lemon.handler

import gg.scala.lemon.Lemon
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/17/2021
 */
object FilterHandler {

    private val regexList = mutableListOf<Regex>()

    private val linkRegexList = mutableListOf(
        "^(http:\\/\\/www\\.|https:\\/\\/www\\.|http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?\$".toRegex()
    )

    init {
        for (regex in Lemon.instance.settings.blacklistedPhraseRegex) {
            regexList.add(regex.toRegex())
        }
    }

    fun checkIfMessageFiltered(
        message: String, sender: Player,
        target: Player? = null,
        type: FilterType = FilterType.PUBLIC
    ): Boolean {
        if (sender.hasPermission("lemon.filter.bypass")) {
            return false
        }

        message.split(" ").forEach { word ->
            val finalWord = word.toLowerCase()
                .replace(oldValue = "@", newValue = "a")
                .replace(oldValue = "3", newValue = "e")
                .replace(oldValue = "0", newValue = "o")
                .replace(oldValue = "4", newValue = "a")
                .replace(oldValue = "1", newValue = "i")
                .replace(oldValue = "5", newValue = "s")

            this.regexList.filter { it.matches(finalWord) }.forEach { _ ->
                return notifyLocally(
                    title = "Blacklisted Phrase",
                    description = "Message contains a phrase: ${CC.WHITE}$finalWord${CC.GRAY}.",
                    message = message, player = sender, type = type,
                    target = target,
                )
            }

            this.linkRegexList.filter { it.matches(finalWord) }.forEach { _ ->
                return notifyLocally(
                    title = "Blacklisted Link",
                    description = "Message contains a link: ${CC.WHITE}$finalWord${CC.GRAY}.",
                    message = message, player = sender, type = type,
                    target = target,
                )
            }
        }

        return false
    }

    private fun notifyLocally(
        title: String, description: String,
        message: String, player: Player,
        target: Player? = null, type: FilterType
    ): Boolean {
        val fancyMessage = FancyMessage()
            .withMessage("${CC.RED}[Filtered] ${
                when (type) {
                    FilterType.PUBLIC -> {
                        // should have rank too
                        "${CC.YELLOW}${player.displayName}${CC.WHITE}:"
                    }
                    FilterType.PRIVATE -> {
                        "${CC.GRAY}(${player.name} -> ${target!!.name})"
                    }
                }
            } $message")
            .andHoverOf(
                "${CC.RED}$title",
                "${CC.GRAY}$description"
            )

        Bukkit.getOnlinePlayers().forEach {
            val lemonPlayer = PlayerHandler.findPlayer(it).orElse(null)

            if (lemonPlayer != null && it.hasPermission("lemon.staff") && !lemonPlayer.getSetting("filtered-messages-disabled")) {
                fancyMessage.sendToPlayer(it)
            }
        }; return true
    }

    enum class FilterType {
        PUBLIC, PRIVATE
    }
}
