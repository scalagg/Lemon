package gg.scala.lemon.handler

import com.solexgames.datastore.commons.connection.impl.mongo.UriMongoConnection
import com.solexgames.datastore.commons.layer.impl.MongoStorageLayer
import com.solexgames.datastore.commons.storage.impl.MongoStorageBuilder
import gg.scala.lemon.Lemon
import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.rank.Rank
import net.evilblock.cubed.serializers.Serializers

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
class DataStoreHandler {

    var lemonPlayerLayer: MongoStorageLayer<LemonPlayer>
    var punishmentLayer: MongoStorageLayer<Punishment>
    var rankLayer: MongoStorageLayer<Rank>
    var grantLayer: MongoStorageLayer<Grant>

    init {
        val mongoConnection = UriMongoConnection(Lemon.instance.mongoConfig.uri)
        val database = Lemon.instance.mongoConfig.database

        this.lemonPlayerLayer = MongoStorageBuilder<LemonPlayer>()
            .setDatabase(database).setCollection("lemon_players")
            .setConnection(mongoConnection).setType(LemonPlayer::class.java).build()

        this.punishmentLayer = MongoStorageBuilder<Punishment>()
            .setDatabase(database).setCollection("lemon_punishments")
            .setConnection(mongoConnection).setType(Punishment::class.java).build()

        this.rankLayer = MongoStorageBuilder<Rank>()
            .setDatabase(database).setCollection("lemon_ranks")
            .setConnection(mongoConnection).setType(Rank::class.java).build()

        this.grantLayer = MongoStorageBuilder<Grant>()
            .setDatabase(database).setCollection("lemon_grants")
            .setConnection(mongoConnection).setType(Grant::class.java).build()

        listOf(
            this.lemonPlayerLayer, this.punishmentLayer,
            this.rankLayer, this.grantLayer
        ).forEach {
            it.supplyWithCustomGson(Serializers.gson)
            it.fetchEntryByKey("testing")
        }
    }
}
