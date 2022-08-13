package gg.scala.lemon.util.minequest

import gg.scala.commons.annotations.plugin.SoftDependency
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.flavor.service.ignore.IgnoreAutoScan

/**
 * @author GrowlyX
 * @since 8/13/2022
 */
@Service
@IgnoreAutoScan
@SoftDependency("MultiLineAPI")
object MinequestMultiLineTest
{
    @Configure
    fun configure()
    {

    }
}
