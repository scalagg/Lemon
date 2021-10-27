package gg.scala.lemon.menu.punishment

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.LemonConstants
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.util.CubedCacheUtil
import gg.scala.lemon.util.SplitUtil
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * @author GrowlyX
 * @since 10/24/2021
 */
class PunishmentSpecificViewMenu(
    private val punishment: Punishment,
    private val fallback: String? = null
) : Menu("Punishment ${Constants.DOUBLE_ARROW_RIGHT} ${
    SplitUtil.splitUuid(punishment.uuid)
}")
{
    override fun getButtons(player: Player): Map<Int, Button>
    {
        return hashMapOf<Int, Button>().also {
            val username = CubedCacheUtil.fetchName(punishment.target)

            it[2] = ItemBuilder(Material.SKULL_ITEM)
                .name("${CC.GREEN}$username's Accounts")
                .addToLore(
                    "${CC.GRAY}View all accounts in",
                    "${CC.GRAY}relation to this player.",
                    "",
                    "${CC.YELLOW}Click to view!"
                )
                .data(3).toButton() { _, _ ->
                    player.closeInventory()
                    player.performCommand("alts $username")
                }

            it[4] = SpecificButton()

            it[6] = ItemBuilder(XMaterial.MAGMA_CREAM)
                .name("${CC.GREEN}$username's Punishments")
                .addToLore(
                    "${CC.GRAY}View all punishments",
                    "${CC.GRAY}in relation to this player.",
                    "",
                    "${CC.YELLOW}Click to view!"
                )
                .toButton() { _, _ ->
                    player.performCommand("c $username")
                }
        }
    }

    override fun onClose(player: Player, manualClose: Boolean)
    {
        if (manualClose && fallback != null)
        {
            Tasks.delayed(1L)
            {
                player.performCommand(
                    fallback
                )
            }
        }
    }

    inner class SpecificButton : Button()
    {
        override fun getButtonItem(player: Player): ItemStack
        {
            val lines = arrayListOf<String>()

            val statusLore = if (punishment.hasExpired) "${CC.YELLOW}(Expired)" else if (!punishment.isRemoved) "${CC.GREEN}(Active)" else "${CC.RED}(Removed)"
            val addedBy = punishment.addedBy?.let {
                CubedCacheUtil.fetchName(it)
            } ?: let {
                LemonConstants.CONSOLE
            }

            lines.add(CC.GREEN + "+ " + TimeUtil.formatIntoCalendarString(Date(punishment.addedAt)))

            if (punishment.hasExpired) {
                lines.add(CC.GRAY + "* " + TimeUtil.formatIntoCalendarString(punishment.expireDate))
            } else if (punishment.isRemoved) {
                lines.add(CC.RED + "- " + TimeUtil.formatIntoCalendarString(Date(punishment.removedAt)))
            }

            lines.add("")
            lines.add("${CC.GRAY}Target: ${CC.WHITE}${CubedCacheUtil.fetchName(punishment.target)}")

            if (!punishment.category.instant) {
                lines.add("${CC.GRAY}Duration: ${CC.WHITE + punishment.durationString}")
            }
            if (punishment.isActive) {
                lines.add("${CC.GRAY}Expire Date: ${CC.WHITE + punishment.expirationString}")
            }

            lines.add("")
            lines.add("${CC.GRAY}Issued By: ${CC.WHITE}$addedBy")
            lines.add("${CC.GRAY}Issued On: ${CC.WHITE}${punishment.addedOn}")
            lines.add("${CC.GRAY}Issued Reason: ${CC.WHITE}${punishment.addedReason}")

            if (punishment.isRemoved) {
                val removedBy = punishment.removedBy?.let {
                    CubedCacheUtil.fetchName(it)
                } ?: let {
                    LemonConstants.CONSOLE
                }

                lines.add("")
                lines.add("${CC.GRAY}Removed By: ${CC.RED}$removedBy")
                lines.add("${CC.GRAY}Removed On: ${CC.RED}${punishment.removedOn}")
                lines.add("${CC.GRAY}Removed Reason: ${CC.RED}${punishment.removedReason}")
            }

            return ItemBuilder(XMaterial.ANVIL)
                .name("$statusLore ${CC.D_GRAY}Viewing Data")
                .addToLore(lines)
                .build()
        }
    }
}
