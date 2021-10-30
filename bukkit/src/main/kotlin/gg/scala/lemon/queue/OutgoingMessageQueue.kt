package gg.scala.lemon.queue

import gg.scala.banana.message.Message
import gg.scala.lemon.Lemon
import net.evilblock.cubed.serializers.Serializers
import java.util.*
import java.util.concurrent.ForkJoinPool

/**
 * Queues, or instantly dispatches [Message]
 * objects to a specified redis channel.
 *
 * @author GrowlyX
 * @since 10/9/2021
 */
open class OutgoingMessageQueue(
    private val channel: String,
    private val delay: Long = 100L
) : Thread()
{

    private val messages = LinkedList<Message>()

    override fun run()
    {
        while (true)
        {
            try
            {
                if (messages.isNotEmpty())
                {
                    if (messages.first != null)
                    {
                        val popped = messages.pop()

                        if (popped != null)
                        {
                            dispatchInternal(popped)
                        }
                    }
                }
            } catch (e: Exception)
            {
                e.printStackTrace()
            } finally
            {
                sleep(delay)
            }
        }
    }

    /**
     * Adds the [message] to the dispatch queue.
     *
     * @see [PriorityQueue]
     */
    fun dispatchSafe(message: Message)
    {
        messages.add(message)
    }

    /**
     * Dispatches the [message] immediately.
     *
     * Should only be used with messages
     * which are deemed urgent.
     */
    fun dispatchUrgently(message: Message)
    {
        dispatchInternal(message)
    }

    private fun dispatchInternal(popped: Message)
    {
        Lemon.instance.banana.useResource {
            it.publish(channel, Serializers.gson.toJson(popped))
            it.close()
        }
    }

}
