package gg.scala.lemon.metadata

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.metadata.language.edit
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.Locale

class NetworkMetadataEditMenu : Menu("Editing network metadata...")
{
    private val cachedConfigModel = NetworkMetadataDataSync.cached()

    private fun previewChatColor(input: String): ChatColor
    {
        val translated = Color.translate(input)
        return when {
            input.startsWith("#") || translated.startsWith("§x") -> nearestChatColor(translated)
            else -> kotlin.runCatching { ChatColor.valueOf(input.uppercase(Locale.getDefault())) }.getOrElse { ChatColor.WHITE }
        }
    }

    private fun previewColorText(input: String): String
    {
        val translated = Color.translate(input)
        val label = when {
            input.startsWith("#") -> input
            translated.startsWith("§x") -> hexFromLegacy(translated)
            else -> input
        }

        return "$translated■ ${CC.WHITE}$label"
    }

    private fun hexFromLegacy(input: String): String
    {
        val normalized = input.removePrefix("§x").replace("§", "")
        return if (normalized.length >= 6) "#${normalized.take(6)}" else input
    }

    private fun nearestChatColor(input: String): ChatColor
    {
        val normalized = input.removePrefix("§x").replace("§", "")
        if (normalized.length < 6) return ChatColor.WHITE

        val red = normalized.substring(0, 2).toInt(16)
        val green = normalized.substring(2, 4).toInt(16)
        val blue = normalized.substring(4, 6).toInt(16)
        val palette = linkedMapOf(
            ChatColor.DARK_RED to intArrayOf(170, 0, 0),
            ChatColor.RED to intArrayOf(255, 85, 85),
            ChatColor.GOLD to intArrayOf(255, 170, 0),
            ChatColor.YELLOW to intArrayOf(255, 255, 85),
            ChatColor.GREEN to intArrayOf(85, 255, 85),
            ChatColor.DARK_GREEN to intArrayOf(0, 170, 0),
            ChatColor.DARK_AQUA to intArrayOf(0, 170, 170),
            ChatColor.AQUA to intArrayOf(85, 255, 255),
            ChatColor.BLUE to intArrayOf(85, 85, 255),
            ChatColor.DARK_PURPLE to intArrayOf(170, 0, 170),
            ChatColor.LIGHT_PURPLE to intArrayOf(255, 85, 255),
            ChatColor.WHITE to intArrayOf(255, 255, 255),
            ChatColor.GRAY to intArrayOf(170, 170, 170)
        )

        return palette.minByOrNull { (_, color) ->
            val redDiff = red - color[0]
            val greenDiff = green - color[1]
            val blueDiff = blue - color[2]
            redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff
        }?.key ?: ChatColor.WHITE
    }

    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>) = 54
    override fun getButtons(player: Player) = mapOf(
        10 to ItemBuilder
            .of(XMaterial.WHITE_WOOL)
            .data(
                ColorUtil
                    .toWoolData(previewChatColor(cachedConfigModel.primary))
                    .toShort()
            )
            .name("${CC.YELLOW}Primary: ${
                previewColorText(cachedConfigModel.primary)
            }")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a primary color")
                    .acceptInput { _, primaryColor ->
                        val translated = Color.translate(primaryColor)
                        cachedConfigModel.primary = translated
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Primary Color to ${previewColorText(primaryColor)}${CC.GREEN}!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        11 to ItemBuilder
            .of(XMaterial.WHITE_WOOL)
            .data(
                ColorUtil
                    .toWoolData(previewChatColor(cachedConfigModel.secondary))
                    .toShort()
            )
            .name("${CC.YELLOW}Secondary: ${
                previewColorText(cachedConfigModel.secondary)
            }")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a secondary color")
                    .acceptInput { _, secondaryColor ->
                        val translated = Color.translate(secondaryColor)
                        cachedConfigModel.secondary = translated
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Secondary Color to ${previewColorText(secondaryColor)}${CC.GREEN}!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        12 to ItemBuilder
            .of(XMaterial.PLAYER_HEAD)
            .owner("Berkcord")
            .name("${CC.YELLOW}Discord: ${CC.WHITE}${cachedConfigModel.discord}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a Discord link")
                    .acceptInput { _, discordLink ->
                        cachedConfigModel.discord = discordLink
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Discord link to $discordLink!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        13 to ItemBuilder
            .of(XMaterial.PAPER)
            .name("${CC.YELLOW}Server Name: ${CC.WHITE}${cachedConfigModel.serverName}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a Server Name")
                    .acceptInput { _, serverName ->
                        cachedConfigModel.serverName = serverName
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Server Name to $serverName!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        22 to ItemBuilder
            .of(XMaterial.OAK_SIGN)
            .name("${CC.YELLOW}Server Domain: ${CC.WHITE}${cachedConfigModel.domain}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a Server Domain")
                    .acceptInput { _, domain ->
                        cachedConfigModel.domain = domain
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Server Domain to $domain!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        23 to ItemBuilder
            .of(XMaterial.STICK)
            .name("${CC.YELLOW}Guild Tags in Name Tags: ${CC.WHITE}${
                if (cachedConfigModel.properties().guildTagsInNametags) "${CC.GREEN}Yes" else "${CC.RED}NO"
            }")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                cachedConfigModel.properties().guildTagsInNametags =
                    !cachedConfigModel.properties().guildTagsInNametags
                NetworkMetadataDataSync.sync(cachedConfigModel)

                Button.playSuccess(player)
                NetworkMetadataEditMenu().openMenu(player)
            },
        14 to ItemBuilder
            .of(XMaterial.CYAN_DYE)
            .name("${CC.YELLOW}Twitter: ${CC.WHITE}${cachedConfigModel.twitter}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a Twitter")
                    .acceptInput { _, twitter ->
                        cachedConfigModel.twitter = twitter
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Twitter to $twitter!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        15 to ItemBuilder
            .of(XMaterial.GOLD_INGOT)
            .name("${CC.YELLOW}Store: ${CC.WHITE}${cachedConfigModel.store}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a Store")
                    .acceptInput { _, twitter ->
                        cachedConfigModel.store = twitter
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Store to $twitter!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        16 to ItemBuilder
            .of(XMaterial.NAME_TAG)
            .name("${CC.YELLOW}Rank Prefix in Name Tags: ${CC.WHITE}${
                if (cachedConfigModel.properties().rankPrefixInNametags) "${CC.GREEN}Yes" else "${CC.RED}NO"
            }")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                cachedConfigModel.properties().rankPrefixInNametags =
                    !cachedConfigModel.properties().rankPrefixInNametags
                NetworkMetadataDataSync.sync(cachedConfigModel)

                Button.playSuccess(player)
                NetworkMetadataEditMenu().openMenu(player)
            },
        19 to ItemBuilder
            .of(XMaterial.END_PORTAL_FRAME)
            .name("${CC.YELLOW}Tab List Sorting: ${CC.WHITE}${
                if (cachedConfigModel.properties().tablistSortingEnabled) "${CC.GREEN}Yes" else "${CC.RED}NO"
            }")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                cachedConfigModel.properties().tablistSortingEnabled =
                    !cachedConfigModel.properties().tablistSortingEnabled
                NetworkMetadataDataSync.sync(cachedConfigModel)

                Button.playSuccess(player)
                NetworkMetadataEditMenu().openMenu(player)
            },
        20 to cachedConfigModel.edit(
            "Forbidden Commands",
            {
                cachedConfigModel.properties().forbiddenCommands
                    .map { it }
                    .toSet()
            },
            Material.matchMaterial("CLOCK") ?: Material.COMPASS
        ) {
            cachedConfigModel.properties().forbiddenCommands =
                it.toMutableSet()
        },
        21 to cachedConfigModel.edit(
            "Allowed Commands When Banned",
            {
                cachedConfigModel.properties().allowedCommandsWhenBanned
                    .map { it }
                    .toSet()
            },
            Material.BARRIER
        ) {
            cachedConfigModel.properties().allowedCommandsWhenBanned =
                it.toMutableSet()
        },
        28 to cachedConfigModel.edit(
            "Temp. Ban Message",
            cachedConfigModel.language()::banMessageTemporary
        ) {
            cachedConfigModel.language().banMessageTemporary = it
        },
        29 to cachedConfigModel.edit(
            "Perm. Ban Message",
            cachedConfigModel.language()::banMessagePermanent
        ) {
            cachedConfigModel.language().banMessagePermanent = it
        },
        30 to cachedConfigModel.edit(
            "Blacklist Message",
            cachedConfigModel.language()::blacklistMessage
        ) {
            cachedConfigModel.language().blacklistMessage = it
        },
        31 to cachedConfigModel.edit(
            "Ban Relation Message",
            cachedConfigModel.language()::banRelationMessage
        ) {
            cachedConfigModel.language().banRelationMessage = it
        },
        32 to cachedConfigModel.edit(
            "Blacklist Relation Message",
            cachedConfigModel.language()::blacklistRelationMessage
        ) {
            cachedConfigModel.language().blacklistRelationMessage = it
        },
        33 to cachedConfigModel.edit(
            "Mute In-Game Message",
            cachedConfigModel.language()::muteMessage
        ) {
            cachedConfigModel.language().muteMessage = it
        },
        34 to cachedConfigModel.edit(
            "Warning In-Game Message",
            cachedConfigModel.language()::warnMessage
        ) {
            cachedConfigModel.language().warnMessage = it
        },
        37 to cachedConfigModel.edit(
            "Kick Message",
            cachedConfigModel.language()::kickMessage
        ) {
            cachedConfigModel.language().kickMessage = it
        },
        38 to ItemBuilder
            .of(XMaterial.REPEATER)
            .name("${CC.YELLOW}Cooldown Addition Message")
            .addToLore(
                cachedConfigModel.language().cooldownDenyMessageAddition,
                "",
                "${CC.GRAY}Click to edit..."
            )
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a new message")
                    .acceptInput { _, addition ->
                        cachedConfigModel.language().cooldownDenyMessageAddition = addition
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Cooldown Addition to $addition!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
    )
}
