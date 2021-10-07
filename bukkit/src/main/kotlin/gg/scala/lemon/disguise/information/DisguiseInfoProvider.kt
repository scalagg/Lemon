package gg.scala.lemon.disguise.information

import com.solexgames.datastore.commons.connection.impl.mongo.UriMongoConnection
import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import com.solexgames.datastore.commons.layer.impl.RedisStorageLayer
import com.solexgames.datastore.commons.storage.impl.MongoStorageBuilder
import com.solexgames.datastore.commons.storage.impl.RedisStorageBuilder
import gg.scala.lemon.Lemon
import net.evilblock.cubed.serializers.Serializers

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
object DisguiseInfoProvider
{

    internal lateinit var disguiseLayer: MongoStorageLayer<DisguiseInfo>
    internal lateinit var activeDisguises: RedisStorageLayer<DisguiseInfo>

    var initialized = false

    fun initialLoad()
    {
        val mongoConnection = UriMongoConnection(Lemon.instance.mongoConfig.uri)
        val database = Lemon.instance.mongoConfig.database

        disguiseLayer = MongoStorageBuilder<DisguiseInfo>()
            .setDatabase(database).setCollection("lemon_disguises")
            .setConnection(mongoConnection).setType(DisguiseInfo::class.java).build()

        activeDisguises = RedisStorageBuilder<DisguiseInfo>()
            .setSection("lemon:disguised")
            .setType(DisguiseInfo::class.java)
            .setConnection(Lemon.instance.getRedisConnection())
            .build()

        disguiseLayer.supplyWithCustomGson(Serializers.gson)

        initialized = true
    }

    /**
     * Retrieves a random disguise info
     * set which is not currently in use.
     *
     * @param lambda lambda which is handled after
     * the disguise info has been fetched.
     *
     * @author GrowlyX
     */
    fun useRandomAvailableDisguise(lambda: (DisguiseInfo?) -> Unit)
    {
        disguiseLayer.fetchAllEntries().thenAccept { allDisguises ->
            activeDisguises.fetchAllEntries().thenAccept {
                val newMap = allDisguises.toMutableMap()

                newMap.filter { entry ->
                    it.containsKey(entry.value.uuid.toString())
                }.forEach {
                    allDisguises.remove(it.value.uuid.toString())
                }

                if (allDisguises.isEmpty())
                {
                    lambda.invoke(DisguiseInfo.NOTHING)
                } else
                {
                    lambda.invoke(
                        allDisguises.values.random()
                    )
                }
            }
        }
    }
}
