package gg.scala.lemon.notebook

import com.cryptomorin.xseries.XMaterial
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
import net.evilblock.cubed.util.bukkit.ItemBuilder
import net.evilblock.cubed.util.nms.MinecraftProtocol
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BookMeta

/**
 * A wrapper for Bukkit's [BookMeta].
 *
 * @author GrowlyX
 * @since 10/16/2021
 */
class WrappedNotebook
{

    private var handle = ItemStack(
        XMaterial.WRITTEN_BOOK.parseMaterial()
    )

    internal var internalTitle = "Not Identified"
    internal var internalMainPageDescription = listOf("Not Identified")

    internal var buffer: ByteBuf? = null

    internal var hasFinalized = false

    internal var shouldGlow = false
    internal var shouldReplace = false

    fun setTitle(title: String)
    {
        internalTitle = title
    }

    fun replace()
    {
        shouldReplace = true
    }

    fun glow()
    {
        shouldGlow = true
    }

    fun setDescription(vararg description: String)
    {
        val immutableList = listOf(*description)
        internalMainPageDescription = Color.translate(immutableList)
    }

    private fun internalFinalize()
    {
        if (buffer == null)
        {
            buffer = Unpooled.buffer(256)
            buffer!!.setByte(0, 0)
            buffer!!.writerIndex(1)
        }

        if (shouldGlow)
        {
            handle = ItemBuilder.copyOf(handle)
                .glow().build()
        }

        val craftStack = NotebookHandler.AS_NMS_COPY.invoke(null, handle)
        val compound = NotebookHandler.TAG_COMPOUND.newInstance()

        NotebookHandler.TAG_COMPOUND_SET_STRING.invoke(compound, "title", internalTitle)
        NotebookHandler.TAG_COMPOUND_SET_STRING.invoke(compound, "author", "Scala")

        val tagList = NotebookHandler.TAG_LIST.newInstance()
        val joined = internalMainPageDescription.joinToString("\n")
        val tagString = NotebookHandler.TAG_STRING_CONSTRUCTOR.newInstance(joined)

        NotebookHandler.TAG_LIST_ADD.invoke(tagList, tagString)

        NotebookHandler.TAG_COMPOUND_SET.invoke(compound, "pages", tagList)
        NotebookHandler.NMS_ITEM_STACK_TAG_FIELD.set(craftStack, compound)

        handle = NotebookHandler.AS_BUKKIT_COPY.invoke(null, craftStack) as ItemStack
    }

    fun open(player: Player)
    {
        if (!hasFinalized) {
            try {
                internalFinalize()
                hasFinalized = true
            } catch (exception: Exception) {
                exception.printStackTrace()
                player.sendMessage("${CC.RED}Sorry, we couldn't open this notebook.")
                return
            }
        }

        val packet = NotebookHandler.CUSTOM_PAYLOAD_CONSTRUCTOR.newInstance(
            "MC|BOpen", NotebookHandler.DATA_SERIALIZER_CONSTRUCTOR.newInstance(buffer)
        )

        val previous = player.inventory.heldItemSlot
        val previousItem = player.inventory.getItem(previous)

        player.inventory.setItem(previous, handle)

        MinecraftProtocol.send(player, packet)

        player.inventory.setItem(previous, previousItem)
    }
}
