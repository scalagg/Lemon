package gg.scala.lemon.channel.channels.staff

import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import gg.scala.lemon.channel.ChatChannelBuilder
import gg.scala.lemon.channel.ChatChannelService
import gg.scala.lemon.handler.PlayerHandler

/**
 * @author GrowlyX
 * @since 4/6/2022
 */
@Service
object StaffChatChannelService
{
    @Configure
    fun configure()
    {
        StaffChatChannelType.values()
            .forEach { register(it) }
    }

    private fun register(
        channelType: StaffChatChannelType
    )
    {
        val composite =
            StaffChatChannelComposite(channelType)

        val channelName = channelType
            .name.lowercase()

        val channel = ChatChannelBuilder
            .newBuilder()
            .import(composite)
            .compose()
            .distribute()
            .triggerPrefixed(
                channelType.prefix
            )
            .allowOnlyIf {
                val lemonPlayer = PlayerHandler
                    .find(it.uniqueId)
                    ?: return@allowOnlyIf false

                it.hasPermission(
                    "lemon.channel.$channelName"
                ) && !lemonPlayer
                    .getSetting(
                        "staff-messages-disabled"
                    )
            }
            .override(1) {
                val lemonPlayer = PlayerHandler
                    .find(it.uniqueId)
                    ?: return@override false

                lemonPlayer
                    .getMetadata("channel")
                    ?.asString() == channelName
            }

        ChatChannelService.register(channel)
    }
}
