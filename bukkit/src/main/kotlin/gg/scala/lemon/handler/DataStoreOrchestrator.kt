package gg.scala.lemon.handler

import gg.scala.lemon.player.LemonPlayer
import gg.scala.lemon.player.comment.Comment
import gg.scala.lemon.player.grant.Grant
import gg.scala.lemon.player.punishment.Punishment
import gg.scala.lemon.player.rank.Rank
import gg.scala.store.controller.DataStoreObjectControllerCache
import net.evilblock.cubed.serializers.Serializers

/**
 * @author GrowlyX
 * @since 9/7/2021
 */
object DataStoreOrchestrator
{
    fun initialLoad()
    {
        DataStoreObjectControllerCache.create<LemonPlayer>(Serializers.gson)
        DataStoreObjectControllerCache.create<Punishment>(Serializers.gson)
        DataStoreObjectControllerCache.create<Rank>(Serializers.gson)
        DataStoreObjectControllerCache.create<Grant>(Serializers.gson)
        DataStoreObjectControllerCache.create<Comment>(Serializers.gson)
    }
}
