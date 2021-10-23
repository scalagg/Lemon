package gg.scala.lemon.animated

import me.lucko.helper.Schedulers
import java.lang.RuntimeException

/**
 * @author GrowlyX
 * @since 10/22/2021
 */
abstract class AnimatedScoreboardTitle : Runnable
{
    var current: String? = null

    private val currentIndex = -1

    override fun run()
    {
        val newIndex = if (currentIndex >= getAnimations().size)
        {
            0
        } else
        {
            currentIndex + 1
        }

        current = try
        {
            val value = getAnimations()[newIndex]
            value
        } catch (ignored: Exception)
        {
            getAnimations()[0]
        }
    }

    fun initialize()
    {
        if (getAnimations().isEmpty())
        {
            throw RuntimeException("Cannot start animation process when the given animations is empty")
        }

        current = getAnimations()[0]

        Schedulers.async().runRepeating(this, 0L, 10L)
    }

    abstract fun getAnimations(): List<String>

}
