package gg.scala.lemon.menu.grant.issue

import com.cryptomorin.xseries.XMaterial
import gg.scala.lemon.handler.PlayerHandler
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.player.rank.Rank
import gg.scala.lemon.util.QuickAccess
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.pagination.PaginatedMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ColorUtil
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.text.TextSplitter
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.InventoryView
import java.util.*

/**
 * @author GrowlyX
 * @since 9/23/2021
 */
class GrantIssueRankMenu(
    private val uuid: UUID,
    private val name: String
) : PaginatedMenu() {

    override fun getPrePaginatedTitle(player: Player): String {
        return "Grant ${Constants.DOUBLE_ARROW_RIGHT} $name ${Constants.DOUBLE_ARROW_RIGHT} Rank"
    }

    override fun getAllPagesButtons(player: Player): Map<Int, Button> {
        return hashMapOf<Int, Button>().also {
            RankHandler.sorted.forEach { rank ->
                it[it.size] = RankButton(rank)
            }
        }
    }

    private inner class RankButton(
        private val rank: Rank
    ) : Button() {

        override fun getName(player: Player): String {
            return rank.getColoredName()
        }

        override fun getMaterial(player: Player): Material {
            return XMaterial.WHITE_WOOL.parseMaterial()!!
        }

        override fun getDamageValue(player: Player): Byte {
            return ColorUtil.CHAT_COLOR_TO_WOOL_DATA[
                    ChatColor.getByChar(rank.color[1]) ?: ChatColor.WHITE
            ]!!.toByte()
        }

        override fun getDescription(player: Player): List<String> {
            return mutableListOf<String>().also {
                val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

                it.add("${CC.GRAY}Priority: ${CC.WHITE}${rank.weight}")
                it.add("${CC.GRAY}Prefix: ${CC.WHITE}${QuickAccess.replaceEmpty(rank.prefix)}")
                it.add("${CC.GRAY}Suffix: ${CC.WHITE}${QuickAccess.replaceEmpty(rank.suffix)}")
                it.add("${CC.GRAY}Visible: ${CC.WHITE}${rank.visible}")
                it.add("")

                if (lemonPlayer != null && lemonPlayer.activeGrant!!.getRank().weight > rank.weight) {
                    it.add("${CC.YELLOW}Click to grant this rank.")
                } else {
                    it.addAll(
                        TextSplitter.split(
                            text = "You must have a priority higher than ${rank.weight} to grant with this rank!",
                            linePrefix = CC.RED
                        )
                    )
                }
            }
        }

        override fun clicked(player: Player, slot: Int, clickType: ClickType, view: InventoryView) {
            val lemonPlayer = PlayerHandler.findPlayer(player).orElse(null)

            if (lemonPlayer != null && lemonPlayer.activeGrant!!.getRank().weight > rank.weight) {
                GrantIssueDurationMenu(uuid, name, rank).openMenu(player)
            } else {
                player.sendMessage("${CC.RED}You're not allowed to grant this rank.")
            }
        }
    }
}