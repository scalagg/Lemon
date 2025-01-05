package gg.scala.lemon.menu.rank.attribute

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.menu.rank.RankManagementMenu
import gg.scala.lemon.menu.rank.attribute.children.ChildrenSelectionMenu
import gg.scala.lemon.menu.rank.attribute.scope.ScopePretextMenu
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.CallbackInputPrompt
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.math.Numbers
import org.bukkit.entity.Player

/**
 * Class created on 1/1/2025

 * @author Max C.
 * @project lemon
 * @website https://solo.to/redis
 */
class RankAttributeEditorMenu(val rank: Rank): Menu()
{
    init
    {
        placeholder = true
        updateAfterClick = true
    }

    override fun size(buttons: Map<Int, Button>): Int = 36

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf(
            4 to ItemBuilder.of(XMaterial.ARROW)
                .name("${CC.RED}Navigate Back")
                .toButton { _, _ ->
                    RankManagementMenu().openMenu(player)
                },

            10 to ItemBuilder.of(XMaterial.OAK_SIGN)
                .name("${CC.GREEN}Display Name")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${if (rank.displayName == null) "${CC.RED}None" else "${CC.WHITE}${rank.displayName}"}",
                    "${CC.GRAY}Left-Click to change the display name of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new display name you want this rank to have:") { input ->
                        rank.displayName = input
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the display name of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }
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
                    }
                },

            12 to ItemBuilder.of(XMaterial.EMERALD)
                .name("${CC.GREEN}Prefix")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${rank.prefix}",
                    "${CC.GRAY}Left-Click to change the prefix of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new prefix you want this rank to have:") { input ->
                        rank.prefix = input
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the prefix of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }
                },

            13 to ItemBuilder.of(XMaterial.DIAMOND)
                .name("${CC.GREEN}Suffix")
                .addToLore(
                    "${CC.GRAY}Currently: ${CC.WHITE}${rank.suffix.ifEmpty { "${CC.RED}None" }}",
                    "${CC.GRAY}Left-Click to change the suffix of this rank!"
                )
                .toButton { _, _ ->
                    CallbackInputPrompt("${CC.GREEN}Enter the new suffix you want this rank to have:") { input ->
                        rank.suffix = input
                        rank.saveAndPushUpdatesGlobally()

                        player.sendMessage("${CC.GREEN}You have changed the prefix of ${rank.getColoredName(true)} ${CC.GREEN}to ${CC.WHITE}\"$input\"")
                    }
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
                }
        )
    }
}