package gg.scala.lemon.channel

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
object ChatChannelService
{
    val channels =
        mutableListOf<ChatChannel>()

    lateinit var default: ChatChannel

    fun registerDefault(
        channel: ChatChannel
    )
    {
        this.default = channel
    }

    fun register(
        channel: ChatChannel
    )
    {
        this.channels += channel
    }
}
