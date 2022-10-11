package gg.scala.lemon.handler

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.rank.Rank
import gg.scala.store.controller.DataStoreObjectControllerCache

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
@IgnoreAutoScan
@Service(name = "ds-orchestrator")
object DataStoreOrchestrator
{
    @Configure
    fun configure()
    {
        listOf(
            LemonPlayer::class, Punishment::class,
            Rank::class, Grant::class
        ).forEach {
            DataStoreObjectControllerCache
                .create(it)
        }
    }
}
