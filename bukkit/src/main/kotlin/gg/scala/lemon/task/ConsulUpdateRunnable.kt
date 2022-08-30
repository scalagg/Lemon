package gg.scala.lemon.task

import gg.scala.commons.annotations.runnables.Repeating
import gg.scala.lemon.Lemon
import gg.scala.lemon.discovery.LemonDiscoveryClient
import me.lucko.helper.promise.ThreadContext

@Repeating(10L, context = ThreadContext.ASYNC)
object ConsulUpdateRunnable : Runnable
{
    override fun run()
    {
        if (Lemon.instance.settings.consulEnabled)
        {
            LemonDiscoveryClient.discovery()
                .agentClient()
                .pass(Lemon.instance.settings.id)
        }
    }
}
