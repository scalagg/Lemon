package gg.scala.lemon.filter.ml

import com.cryptomorin.xseries.XMaterial
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.menu.menus.TextEditorMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.prompt.InputPrompt
import net.evilblock.cubed.util.bukkit.prompt.NumberPrompt
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.Material
import org.bukkit.entity.Player

class ChatMLEditMenu : Menu("Chat ML")
{
    private val cachedConfigModel = ChatMLDataSync.cached()
    init
    {
        placeholder = true
    }

    override fun size(buttons: Map<Int, Button>) = 27
    override fun getButtons(player: Player) = mapOf(
        10 to ItemBuilder
            .of(XMaterial.GOLD_INGOT)
            .name("${CC.YELLOW}Threshold: ${CC.WHITE}${cachedConfigModel.muteThreshold}")
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                NumberPrompt()
                    .withText("Enter a threshold ${CC.GRAY}(example: 90.0)")
                    .acceptInput { number ->
                        cachedConfigModel.muteThreshold = number.toDouble()
                        ChatMLDataSync.sync(cachedConfigModel)

                        Button.playSuccess(player)
                        player.sendMessage("${CC.GREEN}Updated threshold to $number!")
                        ChatMLEditMenu().openMenu(player)
                    }
                    .start(player)
            },
        11 to ItemBuilder
            .of(XMaterial.MAP)
            .name("${CC.YELLOW}Active: ${CC.WHITE}${
                if (cachedConfigModel.enabled) "${CC.GREEN}Yes" else "${CC.RED}No"
            }")
            .toButton { _, _ ->
                Button.playNeutral(player)
                cachedConfigModel.enabled = !cachedConfigModel.enabled
                ChatMLDataSync.sync(cachedConfigModel)
                openMenu(player)
            },
        12 to ItemBuilder
            .of(XMaterial.CLOCK)
            .name("${CC.YELLOW}API Endpoint:")
            .addToLore(
                "${CC.WHITE}${cachedConfigModel.apiEndpoint}"
            )
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                player.sendMessage("${CC.YELLOW}For reference, this is the current endpoint:")
                player.sendMessage(cachedConfigModel.apiEndpoint)

                InputPrompt()
                    .withText("Enter an API endpoint")
                    .acceptInput { _, text ->
                        Button.playNeutral(player)
                        cachedConfigModel.apiEndpoint = text
                        ChatMLDataSync.sync(cachedConfigModel)
                        openMenu(player)
                    }
                    .start(player)
            },
        13 to ItemBuilder
            .of(XMaterial.MAP)
            .name("${CC.YELLOW}Generative AI Prompt:")
            .setLore(
                TextSplitter.split(
                    cachedConfigModel.generativeAIPrompt,
                    CC.WHITE,
                    " "
                )
            )
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                player.sendMessage("${CC.YELLOW}For reference, this is the current prompt:")
                player.sendMessage(cachedConfigModel.generativeAIPrompt)

                InputPrompt()
                    .withText("Enter a generative AI prompt")
                    .acceptInput { _, text ->
                        Button.playNeutral(player)
                        cachedConfigModel.generativeAIPrompt = text
                        ChatMLDataSync.sync(cachedConfigModel)
                        openMenu(player)
                    }
                    .start(player)
            },
        14 to ItemBuilder
            .of(XMaterial.WRITABLE_BOOK)
            .name("${CC.YELLOW}API Keys:")
            .setLore(
                listOf(
                    "${CC.WHITE}Current API Keys:",
                    ""
                ) +
                cachedConfigModel.apiKeys.map { "${CC.WHITE}- $it" }
            )
            .toButton { _, _ ->
                with(
                    object : TextEditorMenu(cachedConfigModel.apiKeys)
                    {
                        override fun getPrePaginatedTitle(player: Player) =
                            "Editing: ChatML API Keys"

                        override fun onClose(player: Player)
                        {

                        }

                        override fun onSave(player: Player, list: List<String>)
                        {
                            cachedConfigModel.apiKeys.clear()
                            cachedConfigModel.apiKeys.addAll(list)

                            ChatMLDataSync.sync(cachedConfigModel)
                            player.sendMessage("${CC.GREEN}Saved API Keys!")
                        }
                    }
                ) {
                    openMenu(player)
                }
            },
        15 to ItemBuilder
            .of(XMaterial.LAPIS_LAZULI)
            .name("${CC.YELLOW}Discord Web Hook:")
            .addToLore(
                "${CC.WHITE}${cachedConfigModel.webHook}"
            )
            .toButton { _, _ ->
                player.closeInventory()
                Button.playNeutral(player)

                player.sendMessage("${CC.YELLOW}For reference, this is the current Discord web hook:")
                player.sendMessage(cachedConfigModel.webHook)

                InputPrompt()
                    .withText("Enter a new Discord Webhook")
                    .acceptInput { _, text ->
                        Button.playNeutral(player)
                        cachedConfigModel.webHook = "https://discord.com/api/webhooks/$text"
                        ChatMLDataSync.sync(cachedConfigModel)
                        openMenu(player)
                    }
                    .start(player)
            }
    )
}
