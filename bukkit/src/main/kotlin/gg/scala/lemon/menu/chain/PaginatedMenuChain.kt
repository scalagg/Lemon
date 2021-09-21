package gg.scala.lemon.menu.chain

import net.evilblock.cubed.menu.pagination.PaginatedMenu
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/20/2021
 */
private class PaginatedMenuChain {

    private val chain = mutableListOf<MenuChainData>()

    fun start() {

    }

    companion object {
        @JvmStatic
        fun create(): PaginatedMenuChain {
            return PaginatedMenuChain()
        }
    }

    class MenuChainData(
        val menu: PaginatedMenu
    ) {
        var onceOpened: (Player) -> Unit = {}
        var onceComplete: (Player) -> Unit = {}
    }
}
