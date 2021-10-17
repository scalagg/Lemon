package gg.scala.lemon.util

import gg.scala.lemon.Lemon
import gg.scala.lemon.adapter.client.PlayerClientAdapter
import org.bukkit.entity.Player

/**
 * @author GrowlyX
 * @since 9/28/2021
 */
object ClientUtil {

    /**
     * Filter through all available client
     * adapters and handle the logic with an
     * adapter applicable to the player.
     *
     * @author GrowlyX
     */
    @JvmStatic
    fun handleApplicableClient(
        player: Player, lambda: (PlayerClientAdapter) -> Unit
    ) {
        val adapter = Lemon.instance.clientAdapters
            .firstOrNull { it.shouldHandle(player) }

        if (adapter != null) {
            lambda.invoke(adapter)
        }
    }
}
