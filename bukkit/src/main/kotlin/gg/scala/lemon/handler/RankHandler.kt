package gg.scala.lemon.handler

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.lemon.minequest
import gg.scala.lemon.player.rank.Rank
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.util.CC
import java.util.*

@Service
@IgnoreAutoScan
object RankHandler
{
    val ranks = mutableMapOf<UUID, Rank>()

    @Configure
    fun configure()
    {
        DataStoreObjectControllerCache
            .findNotNull<Rank>()
            .loadAll(DataStoreStorageType.MONGO)
            .thenAccept { entries ->
                entries.forEach {
                    if (it.value.serverScopes == null)
                    {
                        it.value.serverScopes = mutableListOf()
                    }

                    if (it.value.grantable == null)
                    {
                        it.value.grantable = true
                    }

                    ranks[it.value.uuid] = it.value
                }

                ranks.ifEmpty {
                    createDefaultRank()
                }
            }
    }

    val sorted: List<Rank>
        get()
        {
            return ranks.values.sortedByDescending { it.weight }
        }

    val sortedI: List<Rank>
        get()
        {
            return ranks.values.sortedByDescending { -it.weight }
        }

    fun findRank(uuid: UUID): Rank?
    {
        return ranks.values.firstOrNull {
            it.uuid == uuid
        }
    }

    fun findRank(name: String): Rank?
    {
        return ranks.values.firstOrNull {
            it.name.equals(name, true)
        }
    }

    fun getDefaultRank(): Rank
    {
        return findRank("Default")
            ?: throw IllegalStateException(
                "No default rank found, early call?"
            )
    }

    fun getSortedRankString(): String
    {
        return ranks.values
            .filter { it.visible }
            .sortedBy { -it.weight }
            .joinToString(separator = "${CC.WHITE}, ") {
                if (minequest())
                {
                    if (it.uuid == getDefaultRank().uuid)
                        it.getColoredName()
                    else
                        it.prefix
                } else
                {
                    it.getColoredName()
                }
            }
    }

    private fun createDefaultRank(): Rank
    {
        val rank = Rank(
            UUID.randomUUID(),
            "Default"
        )
        rank.saveAndPushUpdatesGlobally()

        ranks[rank.uuid] = rank

        return rank
    }
}
