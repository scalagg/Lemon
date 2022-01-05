package gg.scala.lemon.menu.frozen

import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.FrozenPlayerHandler
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 1/1/2022
 */
class PlayerFrozenMenu : Menu("You're frozen!")
{
    init
    {
        autoUpdate = true
        placeholder = true
    }

    override fun getButtons(player: Player): Map<Int, Button>
    {
        return mutableMapOf<Int, Button>().also {
            val expirable = FrozenPlayerHandler.expirables[player.uniqueId]
            val description = mutableListOf<String>()

            if (expirable == null) {
                description.add("${CC.RED}Your time is up!")
                description.add("")
                description.add("${CC.GRAY}You've been frozen")
                description.add("${CC.GRAY}for more than five")
                description.add("${CC.GRAY}minutes.")
            } else {
                description.add("${CC.GRAY}You have:")
                description.add("${CC.WHITE}${expirable.durationFromNowStringRaw}")
                description.add("")
                description.add("${CC.GRAY}to join our discord")
                description.add("${CC.GRAY}server:")
                description.add("${CC.WHITE}${LemonConstants.DISCORD_LINK}")
            }

            it[4] = ItemBuilder(Material.INK_SACK)
                .name("${CC.D_RED}You're frozen!")
                .setLore(description)
                .data(1).toButton()

            it[8] = ItemBuilder(Material.NETHER_STAR)
                .name("${CC.D_RED}Disconnect")
                .addToLore(
                    "${CC.GRAY}You may get banned by",
                    "${CC.GRAY}our staff for logging out!",
                    "",
                    "${CC.I_WHITE}Logout at your own discretion!",
                    "",
                    "${CC.D_RED}Staff will receive a notification",
                    "${CC.D_RED}regarding your disconnection.",
                )
                .data(1).toButton { _, _ ->
                    player.kickPlayer("${CC.RED}You've logged out while frozen.")
                }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (player.hasMetadata("frozen"))
        {
            // We're re-opening the menu if the
            // player is still frozen
            Tasks.delayed(1L) { openMenu(player) }
        }
    }
}
