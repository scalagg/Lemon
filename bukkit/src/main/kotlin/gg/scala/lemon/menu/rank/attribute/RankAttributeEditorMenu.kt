package gg.scala.lemon.menu.rank.attribute

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.menu.rank.RankManagementMenu
import gg.scala.lemon.menu.rank.attribute.children.ChildrenSelectionMenu
import gg.scala.lemon.menu.rank.attribute.scope.ScopePretextMenu
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.CallbackInputPrompt
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.math.Numbers
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player

class RankAttributeEditorMenu(val rank: Rank): Menu()
{
    init
    {
        placeholder = true
        updateAfterClick = true
    }

    override fun getTitle(player: Player): String = "Editing ${rank.getColoredName()}${CC.D_GRAY}..."

    override fun size(buttons: Map<Int, Button>): Int = 36

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose) {
            Tasks.sync {
                RankManagementMenu().openMenu(player)
            }
        }
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf(
            4 to ItemBuilder.of(XMaterial.NAME_TAG)
                .name("${CC.RED}Rename Rank")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${"${CC.WHITE}${rank.name}"}",
                    "${CC.GRAY}Left-Click to change the name of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new name you want this rank to have:") { input ->
                        if (input.equals("stop", true) || input.equals("cancel", true))
                        {
                            player.sendMessage("${CC.RED}You've cancelled the rank rename modification.")
                            return@CallbackInputPrompt
                        }

                        rank.name = ChatColor.stripColor(input)
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the name of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }.start(player)
                },

            10 to ItemBuilder.of(XMaterial.OAK_SIGN)
                .name("${CC.GREEN}Display Name")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${if (rank.displayName == null) "${CC.RED}None" else "${CC.WHITE}${rank.displayName}"}",
                    "${CC.GRAY}Left-Click to change the display name of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new display name you want this rank to have:") { input ->
                        if (input.equals("stop", true) || input.equals("cancel", true))
                        {
                            player.sendMessage("${CC.RED}You've cancelled the rank display name modification.")
                            return@CallbackInputPrompt
                        }

                        rank.displayName = input
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the display name of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }.start(player)
                },

            11 to ItemBuilder.of(XMaterial.ANVIL)
                .name("${CC.GREEN}Weight")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${Numbers.format(rank.weight)}",
                    "${CC.GRAY}Left-Click to change the weight of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new weight you want this rank to have:") { input ->
                        val weight: Int

                        if (input.equals("stop", true) || input.equals("cancel", true))
                        {
                            player.sendMessage("${CC.RED}You've cancelled the rank weight modification.")
                            return@CallbackInputPrompt
                        }

                        try
                        {
                            weight = Integer.parseInt(input)
                        } catch (e: Exception)
                        {
                            player.sendMessage("${CC.RED}This is not a valid integer!")
                            return@CallbackInputPrompt
                        }

                        rank.weight = weight
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the display name of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }.start(player)
                },

            12 to ItemBuilder.of(XMaterial.EMERALD)
                .name("${CC.GREEN}Prefix")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${QuickAccess.replaceEmpty(rank.prefix)}",
                    "${CC.GRAY}Left-Click to change the prefix of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new prefix you want this rank to have:") { input ->
                        if (input.equals("stop", true) || input.equals("cancel", true))
                        {
                            player.sendMessage("${CC.RED}You've cancelled the rank prefix modification.")
                            return@CallbackInputPrompt
                        }

                        rank.prefix = input
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the prefix of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }.start(player)
                },

            13 to ItemBuilder.of(XMaterial.DIAMOND)
                .name("${CC.GREEN}Color")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${QuickAccess.replaceEmpty(rank.color)}this",
                    "${CC.GRAY}Left-Click to change the color of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new color you want this rank to have:") { input ->
                        if (input.equals("stop", true) || input.equals("cancel", true))
                        {
                            player.sendMessage("${CC.RED}You've cancelled the rank prefix modification.")
                            return@CallbackInputPrompt
                        }

                        rank.color = input
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the color of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }.start(player)
                },

            14 to ItemBuilder.of(XMaterial.GLASS)
                .name("${CC.GREEN}Visibility")
                .addToLore(
                    "${CC.GRAY}Currently: ${if (rank.visible) "${CC.D_GREEN}True" else "${CC.RED}False"}",
                    "${CC.GRAY}Left-Click to change the visibility of this rank!"
                )
                .toButton { _, _ ->
                    rank.visible = !rank.visible
                    rank.saveAndPushUpdatesGlobally()

                    player.sendMessage("${CC.GREEN}You have changed the visibility of ${rank.getColoredName(true)} ${CC.GREEN}to ${if (rank.visible) "${CC.D_GREEN}True" else "${CC.RED}False"}")
                },

            15 to ItemBuilder.of(XMaterial.COAL_BLOCK)
                .name("${CC.GREEN}Grantable")
                .addToLore(
                    "${CC.GRAY}Currently: ${if (rank.grantable()) "${CC.D_GREEN}True" else "${CC.RED}False"}",
                    "${CC.GRAY}Left-Click to change the grantable status of this rank!"
                )
                .toButton { _, _ ->
                    rank.grantable = !rank.grantable()
                    rank.saveAndPushUpdatesGlobally()

                    player.sendMessage("${CC.GREEN}You have changed the grantable status of ${rank.getColoredName(true)} ${CC.GREEN}to ${if (rank.grantable()) "${CC.D_GREEN}True" else "${CC.RED}False"}")
                },

            16 to ItemBuilder.of(XMaterial.COMPASS)
                .name("${CC.GREEN}Scopes")
                .addToLore(
                    "${CC.GRAY}Left-Click to edit the scopes of this rank!"
                )
                .toButton { _, _ ->
                    ScopePretextMenu(rank).openMenu(player)
                },

            22 to ItemBuilder.of(XMaterial.GHAST_SPAWN_EGG)
                .name("${CC.GREEN}Children")
                .addToLore(
                    "${CC.GRAY}Left-Click to edit the children of this rank!"
                )
                .toButton { _, _ ->
                    ChildrenSelectionMenu(rank).openMenu(player)
                },

            35 to ItemBuilder.of(XMaterial.ARROW)
                .name("${CC.RED}Navigate Back")
                .toButton { _, _ ->
                    RankManagementMenu().openMenu(player)
                }
        )
    }
}
