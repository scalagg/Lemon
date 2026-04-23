package gg.scala.lemon.listener

import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.Lemon
import me.lucko.helper.Events
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventException
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.EventExecutor

@Service
object ChatEventBridgeService : Listener
{
    @Inject
    lateinit var plugin: Lemon

    @Configure
    fun configure()
    {
        val asyncChatClass = runCatching {
            Class.forName("io.papermc.paper.event.player.AsyncChatEvent") as Class<out Event>
        }.getOrNull()

        if (asyncChatClass == null)
        {
            Events.subscribe(AsyncPlayerChatEvent::class.java, EventPriority.MONITOR)
                .filter { !it.isCancelled }
                .handler(PlayerListener::processChat)
                .bindWith(plugin)
            return
        }

        Bukkit.getPluginManager().registerEvent(
            asyncChatClass,
            this,
            EventPriority.MONITOR,
            EventExecutor { _, event ->
                if (event.javaClass != asyncChatClass) return@EventExecutor

                try
                {
                    val cancelled = event.javaClass.getMethod("isCancelled").invoke(event) as Boolean
                    if (cancelled) return@EventExecutor

                    val player = event.javaClass.getMethod("getPlayer").invoke(event) as org.bukkit.entity.Player
                    val component = event.javaClass.getMethod("message").invoke(event) as net.kyori.adventure.text.Component
                    val message = ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(component))
                    val synthetic = AsyncPlayerChatEvent(true, player, message, Bukkit.getOnlinePlayers().toMutableSet())

                    PlayerListener.processChat(synthetic)
                    event.javaClass.getMethod("setCancelled", Boolean::class.javaPrimitiveType).invoke(event, true)
                }
                catch (exception: Exception)
                {
                    throw EventException(exception)
                }
            },
            plugin,
            true
        )
    }
}
