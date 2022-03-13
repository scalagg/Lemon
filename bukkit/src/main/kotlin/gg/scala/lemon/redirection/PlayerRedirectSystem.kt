package gg.scala.lemon.redirection

import gg.scala.aware.conversation.ConversationContinuation
import gg.scala.aware.conversation.ConversationFactoryBuilder
import gg.scala.lemon.Lemon
import me.lucko.helper.Events
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.FancyMessage
import net.jodah.expiringmap.ExpirationPolicy
import net.jodah.expiringmap.ExpiringMap
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import java.io.Closeable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A re-write of helper's
 * [AbstractRedirectSystem].
 *
 * @author GrowlyX
 * @since 3/13/2022
 */
open class PlayerRedirectSystem<T>(
    val handler: PlayerRedirectHandler,
    private val assistant: PlayerRedirect<T>
) : Closeable
{
    companion object
    {
        @JvmStatic
        lateinit var INSTANCE: PlayerRedirectSystem<*>
    }

    private var ensureJoinRedirection = false

    private val expected = ExpiringMap.builder()
        .expiration(5, TimeUnit.SECONDS)
        .expirationPolicy(ExpirationPolicy.CREATED)
        .build<UUID, PlayerRedirectMessageResponse>()

    private val conversation by lazy {
        ConversationFactoryBuilder
            .of<PlayerRedirectMessage, PlayerRedirectMessageResponse>()
            .channel("player-redirection")
            .timeout(2L, TimeUnit.SECONDS) {
                val player = this.assistant
                    .retrieve(it.player)
                    ?: return@timeout

                val message = FancyMessage()
                    .apply {
                        withMessage("${prefix}We were unable to connect you to ${CC.B_RED}${it.server}${CC.RED}.")
                    }

                this.assistant
                    .sendMessage(player, message)
            }
            .response { message ->
                val serverId = Lemon.instance
                    .localInstance.serverId

                if (serverId != message.server)
                    return@response PlayerRedirectMessageResponse(
                        message.uniqueId, "", false, "", true
                    )

                // this is already handled
                // asynchronously, so we can
                // join the future
                val processed = handler
                    .process(message).join()
                    .wrap(message.uniqueId)

                if (processed.allowed)
                {
                    expected[message.player] = processed
                }

                return@response processed
            }
            .receive { message, response ->
                if (response.empty)
                    return@receive ConversationContinuation.CONTINUE

                if (message.server != response.server)
                    return@receive ConversationContinuation.CONTINUE

                val player = this.assistant
                    .retrieve(message.player)
                    ?: return@receive ConversationContinuation.END

                if (response.allowed)
                {
                    this.assistant.redirect(player, message)
                } else
                {
                    val responseMessage = FancyMessage()
                        .apply {
                            withMessage("${prefix}${CC.RED} We were unable to connect you to ${CC.B_RED}${message.server}${CC.RED} due to ${CC.WHITE}${
                                response.allowedMessage
                            }.")
                        }

                    this.assistant
                        .sendMessage(player, responseMessage)

                    return@receive ConversationContinuation.END
                }

                return@receive ConversationContinuation.END
            }
            .build()
    }

    fun redirect(t: T, server: String)
    {
        val message = PlayerRedirectMessage(
            this.assistant.retrieve(t), server, mutableMapOf()
        )

        this.conversation.distribute(message)
    }

    fun configure()
    {
        INSTANCE = this

        this.conversation
            .configure()
            .toCompletableFuture()
            .join()

        Events.subscribe(AsyncPlayerPreLoginEvent::class.java)
            .filter { ensureJoinRedirection }
            .handler {
                val response = expected[it.uniqueId]

                if (response == null || !response.allowed)
                {
                    it.disallow(
                        AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                        """
                            ${CC.RED}Sorry, we're unable to process your login:
                            ${response?.allowedMessage ?: "No redirection attempt"}
                        """.trimIndent()
                    )
                }
            }
            .bindWith(Lemon.instance)
    }

    override fun close()
    {
        this.conversation.close()
    }
}
