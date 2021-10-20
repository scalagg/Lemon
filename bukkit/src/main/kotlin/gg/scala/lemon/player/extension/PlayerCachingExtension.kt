package gg.scala.lemon.player.extension

import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.FundamentalLemonPlayer
import gg.scala.lemon.player.LemonPlayer
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author GrowlyX
 * @since 10/19/2021
 */
object PlayerCachingExtension
{
    private lateinit var handle: RedisStorageLayer<FundamentalLemonPlayer>

    var loaded = false

    fun initialLoad()
    {
        handle = RedisStorageLayer(
            Lemon.instance.redisConnection,
            "lemon:players",
            FundamentalLemonPlayer::class.java
        )

        loaded = true
    }

    fun retrieve(uniqueId: UUID): CompletableFuture<FundamentalLemonPlayer?>
    {
        return handle.fetchEntryByKey(uniqueId.toString())
    }

    fun memorize(lemonPlayer: LemonPlayer)
    {
        if (!loaded) return

        val fundamental = FundamentalLemonPlayer(
            lemonPlayer.uniqueId, lemonPlayer.name
        )

        fundamental.currentServer = Lemon.instance.settings.id
        fundamental.currentDisplayName = lemonPlayer.getColoredName()

        handle.saveEntry(
            lemonPlayer.uniqueId.toString(), fundamental
        )
    }

    fun forget(lemonPlayer: LemonPlayer)
    {
        if (!loaded) return

        handle.deleteEntry(
            lemonPlayer.uniqueId.toString()
        )
    }
}
