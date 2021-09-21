package gg.scala.lemon.player.event

import net.evilblock.cubed.util.bukkit.Tasks.sync
import org.bukkit.Bukkit
import org.bukkit.event.Cancellable
import org.bukkit.event.Event

/**
 * @author GrowlyX
 * @since 9/21/2021
 */
abstract class LemonEvent : Event(), Cancellable {

    private var cancelled = false

    abstract fun ifNotCancelled(): () -> Unit

    fun dispatch() {
        sync {
            Bukkit.getPluginManager().callEvent(this)

            if (!isCancelled) {
                ifNotCancelled().invoke()
            }
        }
    }

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(value: Boolean) {
        cancelled = value
    }
}
