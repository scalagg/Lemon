package gg.scala.lemon.network.redirector

import gg.scala.lemon.Lemon
import gg.scala.lemon.network.SyncLemonInstanceData
import me.lucko.helper.network.redirect.AbstractRedirectSystem

/**
 * @author GrowlyX
 * @since 1/17/2022
 */
class LemonRedirectSystem : AbstractRedirectSystem(
    Lemon.instance.messenger,
    SyncLemonInstanceData,
    LemonPlayerRedirector
)
