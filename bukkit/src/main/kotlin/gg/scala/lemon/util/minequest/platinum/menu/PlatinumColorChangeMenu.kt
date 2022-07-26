package gg.scala.lemon.util.minequest.platinum.menu

import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.metadata.Metadata
import gg.scala.lemon.util.minequest.platinum.MinequestPlatinumColors
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 7/25/2022
 */
class PlatinumColorChangeMenu : Menu(
    "Platinum Rank Color"
)
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        val buttons = mutableMapOf<Int, Button>()

        val lemonPlayer = PlayerHandler
            .find(player.uniqueId)
            ?: return buttons

        val current = lemonPlayer
            .getMetadata("platinum")
            ?.asString() ?: "default"

        MinequestPlatinumColors.forEach {
            buttons[it.value.menuPosition] = ItemBuilder
                .of(Material.STAINED_GLASS_PANE)
                .data(it.value.paneColor.toShort())
                .name(it.value.translated)
                .apply {
                    if (current == it.key)
                    {
                        addToLore("${CC.RED}Already equipped!")
                    } else
                    {
                        addToLore("${CC.GREEN}Click to equip!")
                    }
                }
                .toButton { _, _ ->
                    if (current == it.key)
                    {
                        player.sendMessage("${CC.RED}You already have this platinum color equipped!")
                        return@toButton
                    }

                    lemonPlayer.updateOrAddMetadata(
                        "platinum", Metadata(it.key)
                    )
                    lemonPlayer.save()

                    player.sendMessage(
                        "${CC.GREEN}You updated your platinum color to: ${CC.WHITE}${it.value.translated}${CC.GREEN}!"
                    )
                }
        }

        return buttons
    }
}
