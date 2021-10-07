package gg.scala.lemon.handler

import gg.scala.lemon.task.ShutdownRunnable
import net.evilblock.cubed.acf.ConditionFailedException

object ServerHandler
{

    var shutdownRunnable: ShutdownRunnable? = null

    fun initiateShutdown(seconds: Int)
    {
        if (shutdownRunnable != null)
        {
            throw ConditionFailedException("A server shutdown has already been initialized.")
        }

        shutdownRunnable = ShutdownRunnable(seconds)
    }

    fun cancelShutdown()
    {
        if (shutdownRunnable == null)
        {
            throw ConditionFailedException("There is currently no scheduled shutdown.")
        }

        shutdownRunnable!!.cancel()
        shutdownRunnable = null
    }
}
