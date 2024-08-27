package gg.scala.lemon.metadata

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
class NetworkMetadataEditMenu : Menu("Editing network meta...")
{
    private val cachedConfigModel = NetworkMetadataDataSync.cached()
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>) = 36
    override fun getButtons(player: Player) = mapOf(
        10 to ItemBuilder
            .of(Material.PAPER)
            .name("${CC.YELLOW}Discord: ${CC.WHITE}${cachedConfigModel.discord}")
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
        11 to ItemBuilder
            .of(Material.WOOL)
            .data(
                ColorUtil
                .toWoolData(ChatColor.valueOf(cachedConfigModel.primary))
                .toShort()
            )
            .name("${CC.YELLOW}Primary: ${
                ChatColor.valueOf(cachedConfigModel.primary)
            }${cachedConfigModel.primary}")
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
        12 to ItemBuilder
            .of(Material.WOOL)
            .data(
                ColorUtil
                    .toWoolData(ChatColor.valueOf(cachedConfigModel.secondary))
                    .toShort()
            )
            .name("${CC.YELLOW}Secondary: ${
                ChatColor.valueOf(cachedConfigModel.secondary)
            }${cachedConfigModel.secondary}")
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
        13 to ItemBuilder
            .of(Material.PAPER)
            .name("${CC.YELLOW}Server Name: ${CC.WHITE}${cachedConfigModel.serverName}")
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
            .of(Material.PAPER)
            .name("${CC.YELLOW}Twitter: ${CC.WHITE}${cachedConfigModel.twitter}")
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
            .of(Material.PAPER)
            .name("${CC.YELLOW}Store: ${CC.WHITE}${cachedConfigModel.store}")
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
        19 to ItemBuilder
            .of(Material.PAPER)
            .name("${CC.YELLOW}Rank Prefix in Name Tags: ${CC.WHITE}${
                if (cachedConfigModel.properties().rankPrefixInNametags) "${CC.GREEN}Yes" else "${CC.RED}NO"
            }")
            .toButton { _, _ ->
                cachedConfigModel.properties().rankPrefixInNametags =
                    !cachedConfigModel.properties().rankPrefixInNametags
                NetworkMetadataDataSync.sync(cachedConfigModel)

                Button.playSuccess(player)
                NetworkMetadataEditMenu().openMenu(player)
            },
        20 to ItemBuilder
            .of(Material.PAPER)
            .name("${CC.YELLOW}Tab List Sorting: ${CC.WHITE}${
                if (cachedConfigModel.properties().tablistSortingEnabled) "${CC.GREEN}Yes" else "${CC.RED}NO"
            }")
            .toButton { _, _ ->
                cachedConfigModel.properties().tablistSortingEnabled =
                    !cachedConfigModel.properties().tablistSortingEnabled
                NetworkMetadataDataSync.sync(cachedConfigModel)

                Button.playSuccess(player)
                NetworkMetadataEditMenu().openMenu(player)
            }
    )
}
