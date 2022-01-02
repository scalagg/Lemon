package gg.scala.lemon.disguise.information

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan
import gg.scala.store.controller.DataStoreObjectControllerCache
import gg.scala.store.storage.type.DataStoreStorageType
import net.evilblock.cubed.serializers.Serializers
import net.evilblock.cubed.util.bukkit.Tasks

/**
 * @author GrowlyX
 * @since 9/29/2021
 */
@Service
@IgnoreAutoScan
object DisguiseInfoProvider
{
    var initialized = false

    @Configure
    fun configure()
    {
        DataStoreObjectControllerCache.create<DisguiseInfo>(Serializers.gson)

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
        val controller = DataStoreObjectControllerCache.findNotNull<DisguiseInfo>()

        controller.loadAll(DataStoreStorageType.MONGO).thenAccept { allDisguises ->
            controller.loadAll(DataStoreStorageType.REDIS).thenAccept {
                val mutableMap = it.toMutableMap()
                val newMap = allDisguises.toMutableMap()

                newMap.filter { entry ->
                    mutableMap.containsKey(entry.value.uuid)
                }.forEach { entry ->
                    mutableMap.remove(entry.value.uuid)
                }

                Tasks.sync {
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
}
