package gg.scala.lemon.handler

import com.google.gson.GsonBuilder
import com.google.gson.LongSerializationPolicy
import gg.scala.flavor.service.Close
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.comment.Comment
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.rank.Rank
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.serializer.DataStoreSerializer
import gg.scala.store.serializer.serializers.GsonSerializer
import gg.scala.store.storage.storable.IDataStoreObject
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.serializers.impl.UUIDAdapter
import java.util.UUID
import kotlin.reflect.KClass

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
            Rank::class, Grant::class, Comment::class
        ).forEach {
            DataStoreObjectControllerCache
                .create(it)
        }

        val expose = GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .setLongSerializationPolicy(
                LongSerializationPolicy.STRING
            )
            .registerTypeAdapter(
                UUID::class.java,
                UUIDAdapter
            )
            .create()

        DataStoreObjectControllerCache
            .findNotNull<LemonPlayer>()
            .useSerializer(
                object : DataStoreSerializer
                {
                    override fun <T : Any> deserialize(
                        `class`: KClass<T>, input: String
                    ) = expose.fromJson(input, `class`.java)

                    override fun serialize(
                        `object`: Any
                    ) = expose.toJson(`object`)
                }
            )
    }

    @Close
    fun close()
    {
        DataStoreObjectControllerCache.closeAll()
    }
}
