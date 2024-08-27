package gg.scala.lemon.metadata

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.metadata.language.edit
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/21/2024
 */
class NetworkMetadataEditMenu : Menu("Editing network metadata...")
{
    private val cachedConfigModel = NetworkMetadataDataSync.cached()
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>) = 54
    override fun getButtons(player: Player) = mapOf(
        10 to ItemBuilder
            .of(Material.WOOL)
            .data(
                ColorUtil
                    .toWoolData(ChatColor.valueOf(cachedConfigModel.primary))
                    .toShort()
            )
            .name("${CC.YELLOW}Primary: ${
                ChatColor.valueOf(cachedConfigModel.primary)
            }${cachedConfigModel.primary}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a primary color")
                    .acceptInput { _, primaryColor ->
                        val primaryChatColor = kotlin
                            .runCatching { ChatColor.valueOf(primaryColor) }
                            .getOrNull()
                            ?: return@acceptInput run {
                                player.sendMessage("${CC.RED}Invalid color!")
                            }

                        cachedConfigModel.primary = primaryChatColor.name
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Primary Color to $primaryChatColor${primaryChatColor.name}!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        11 to ItemBuilder
            .of(Material.WOOL)
            .data(
                ColorUtil
                    .toWoolData(ChatColor.valueOf(cachedConfigModel.secondary))
                    .toShort()
            )
            .name("${CC.YELLOW}Secondary: ${
                ChatColor.valueOf(cachedConfigModel.secondary)
            }${cachedConfigModel.secondary}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a secondary color")
                    .acceptInput { _, secondaryColor ->
                        val secondaryChatColor = kotlin
                            .runCatching { ChatColor.valueOf(secondaryColor) }
                            .getOrNull()
                            ?: return@acceptInput run {
                                player.sendMessage("${CC.RED}Invalid color!")
                            }

                        cachedConfigModel.secondary = secondaryChatColor.name
                        NetworkMetadataDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated Secondary Color to $secondaryChatColor${secondaryChatColor.name}!")
                        NetworkMetadataEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        12 to ItemBuilder
            .of(XMaterial.CYAN_DYE)
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
            .of(Material.PAPER)
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
        14 to ItemBuilder
            .of(Material.RAW_FISH)
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
            .of(Material.GOLD_INGOT)
            .name("${CC.YELLOW}Store: ${CC.WHITE}${cachedConfigModel.store}")
            .addToLore("${CC.GRAY}Click to edit...")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                InputPrompt()
                    .withText("Enter a Twitter")
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
            .of(Material.NAME_TAG)
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
            .of(Material.ENDER_PORTAL_FRAME)
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
            .of(Material.SIGN)
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
