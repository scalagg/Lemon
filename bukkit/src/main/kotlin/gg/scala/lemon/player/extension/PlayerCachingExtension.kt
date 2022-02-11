package gg.scala.lemon.player.extension

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.Lemon
import gg.scala.lemon.handler.RankHandler
import gg.scala.lemon.player.FundamentalLemonPlayer
import gg.scala.lemon.player.LemonPlayer
import gg.scala.store.controller.DataStoreObjectController
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.serializers.Serializers
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/19/2021
 */
@Service(name = "player_redis_cache")
@IgnoreAutoScan
object PlayerCachingExtension
{
    lateinit var controller: DataStoreObjectController<FundamentalLemonPlayer>

    @Configure
    fun configure()
    {
        controller = DataStoreObjectControllerCache.create()
        controller.provideCustomSerializer(Serializers.gson)
    }

    fun retrieve(uniqueId: UUID): CompletableFuture<FundamentalLemonPlayer?>
    {
        return controller.load(uniqueId, DataStoreStorageType.REDIS)
    }

    fun memorize(lemonPlayer: LemonPlayer): CompletableFuture<Void>
    {
        val fundamental = FundamentalLemonPlayer(
            lemonPlayer.uniqueId, lemonPlayer.name
        )

        fundamental.currentServer = Lemon.instance.settings.id
        fundamental.currentDisplayName = lemonPlayer.getColoredName()

        fundamental.currentRank = lemonPlayer.activeGrant?.getRank()?.uuid
            ?: RankHandler.getDefaultRank().uuid

        return controller.save(fundamental, DataStoreStorageType.REDIS)
    }

    fun forget(lemonPlayer: LemonPlayer): CompletableFuture<Void>
    {
        return controller.delete(lemonPlayer.uniqueId, DataStoreStorageType.REDIS)
    }
}
