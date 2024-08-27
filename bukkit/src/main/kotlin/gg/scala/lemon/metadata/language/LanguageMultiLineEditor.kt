package gg.scala.lemon.metadata.language

import gg.scala.common.metadata.NetworkMetadata
import gg.scala.lemon.metadata.NetworkMetadataDataSync
import gg.scala.lemon.metadata.NetworkMetadataEditMenu
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.menus.TextEditorMenu
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Material
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 8/27/2024
 */
fun NetworkMetadata.edit(name: String, supplier: () -> Set<String>, update: NetworkMetadata.(Set<String>) -> Unit) = ItemBuilder
    .of(Material.BOOK)
    .name("${CC.YELLOW}$name")
    .setLore(supplier())
    .addToLore(
        "",
        "${CC.GREEN}Click to edit..."
    )
    .toButton { player, _ ->
        val menu = object : TextEditorMenu(supplier())
        {
            override fun getPrePaginatedTitle(player: Player) = "Editing..."

            override fun onClose(player: Player)
            {
                Tasks.delayed(1L) {
                    NetworkMetadataEditMenu().openMenu(player)
                }
            }

            override fun onSave(player: Player, list: List<String>)
            {
                val cached = this@edit
                update(cached, list.toSet())

                NetworkMetadataDataSync.sync(cached)
                    .thenRun {
                        player.sendMessage("${CC.GREEN}Saved!")
                    }
            }
        }

        Button.playNeutral(player!!)
        menu.openMenu(player)
    }
