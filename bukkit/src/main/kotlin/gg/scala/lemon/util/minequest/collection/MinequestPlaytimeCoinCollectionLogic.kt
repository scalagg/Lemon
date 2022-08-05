package gg.scala.lemon.util.minequest.collection

import gg.scala.commons.terminable.PlayerCompositeTerminable
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import me.lucko.helper.Events
import me.lucko.helper.Schedulers
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import java.util.concurrent.TimeUnit

/**
 * @author GrowlyX
 * @since 8/4/2022
 */
@Service
@IgnoreAutoScan
object MinequestPlaytimeCoinCollectionLogic
{
    @Inject
    lateinit var plugin: Lemon

    @Configure
    fun configure()
    {
        Events
            .subscribe(
                PlayerJoinEvent::class.java,
                EventPriority.LOWEST
            )
            .handler {
                Schedulers.async()
                    .runRepeating(
                        { task ->
                            if (!it.player.isOnline)
                            {
                                task.closeSilently()
                                return@runRepeating
                            }

                            MinequestCoinCollectionLogic
                                .onHubCollection.invoke(it.player)
                        },
                        10L, TimeUnit.MINUTES,
                        10L, TimeUnit.MINUTES
                    )
                    .bindWith(
                        PlayerCompositeTerminable.of(it.player)
                    )
            }
            .bindWith(plugin)
    }
}
