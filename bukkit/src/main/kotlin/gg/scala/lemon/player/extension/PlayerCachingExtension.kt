package gg.scala.lemon.player.extension

import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import com.solexgames.datastore.commons.storage.impl.RedisStorageBuilder
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.FundamentalLemonPlayer
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.server.ServerInstance
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.bukkit.Tasks
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
        val builder = RedisStorageBuilder<FundamentalLemonPlayer>()

        builder.setConnection(Lemon.instance.redisConnection)
        builder.setSection("lemon:players")
        builder.setType(FundamentalLemonPlayer::class.java)

        handle = builder.build()

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

        Tasks.sync {
            handle.saveEntry(lemonPlayer.uniqueId.toString(), fundamental).join()
        }
    }

    fun forget(lemonPlayer: LemonPlayer)
    {
        if (!loaded) return

        handle.deleteEntry(
            lemonPlayer.uniqueId.toString()
        )
    }
}
