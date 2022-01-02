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
import net.evilblock.cubed.util.bukkit.Tasks
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/19/2021
 */
@Service
@IgnoreAutoScan
object PlayerCachingExtension
{
    var loaded = false
    lateinit var controller: DataStoreObjectController<FundamentalLemonPlayer>

    @Configure
    fun configure()
    {
        controller = DataStoreObjectControllerCache.create()
        controller.provideCustomSerializer(Serializers.gson)

        loaded = true
    }

    fun retrieve(uniqueId: UUID): CompletableFuture<FundamentalLemonPlayer?>
    {
        return controller.load(uniqueId, DataStoreStorageType.REDIS)
    }

    fun memorize(lemonPlayer: LemonPlayer)
    {
        if (!loaded)
        {
            return
        }

        val fundamental = FundamentalLemonPlayer(
            lemonPlayer.uniqueId, lemonPlayer.name
        )

        fundamental.currentServer = Lemon.instance.settings.id
        fundamental.currentDisplayName = lemonPlayer.getColoredName()

        fundamental.currentRank = lemonPlayer.activeGrant?.getRank()?.uuid
            ?: RankHandler.getDefaultRank().uuid

        Tasks.sync {
            controller.save(fundamental, DataStoreStorageType.REDIS)
        }
    }

    fun forget(lemonPlayer: LemonPlayer)
    {
        if (!loaded) return

        controller.delete(lemonPlayer.uniqueId, DataStoreStorageType.REDIS)
    }
}
