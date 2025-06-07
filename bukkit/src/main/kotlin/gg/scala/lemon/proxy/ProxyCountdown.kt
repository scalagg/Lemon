package gg.scala.lemon.proxy

import gg.scala.lemon.util.other.Expirable

/**
 * @author GrowlyX
 * @since 12/17/2021
 */
class ProxyCountdown : Expirable(System.currentTimeMillis(), 0L)
{
    var active = false

    var presenceStaging = "Something happens in [detailed]"
    var presenceCommenced = "Something just happened"
}
