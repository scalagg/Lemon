package gg.scala.lemon.feature

import gg.scala.cloudsync.shared.discovery.CloudSyncDiscoveryService
import gg.scala.commons.agnostic.sync.ServerSync
import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan

/**
 * @author GrowlyX
 * @since 4/26/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("cloudsync")
object CloudSyncFeature
{
    @Configure
    fun configure()
    {
        CloudSyncDiscoveryService
            .discoverable.assets
            .apply {
                add("gg.scala.lemon:bukkit:Lemon${
                    if ("dev" in ServerSync.getLocalGameServer().groups) ":gradle-dev" else ""
                }")
            }
    }
}
