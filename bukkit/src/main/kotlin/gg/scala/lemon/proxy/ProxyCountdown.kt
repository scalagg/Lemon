package gg.scala.lemon.proxy

import gg.scala.lemon.util.other.Expirable

class ProxyCountdown : Expirable(System.currentTimeMillis(), 0L)
{
    var active = false

    var presenceStaging = "Something happens in [detailed]"
    var presenceCommenced = "Something just happened"
}
