package gg.scala.lemon.notebook

import com.cryptomorin.xseries.XMaterial
import io.netty.buffer.Unpooled
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.Color
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

    internal var notebook = ItemStack(
        XMaterial.WRITTEN_BOOK.parseMaterial()
    )

    var internalTitle = "Not Identified"
    var internalMainPageDescription = listOf("Not Identified")

    var hasFinalized = false

    fun setTitle(title: String)
    {
        internalTitle = title
    }

    fun setDescription(vararg description: String)
    {
        val immutableList = listOf(*description)
        internalMainPageDescription = Color.translate(immutableList)
    }

    internal fun internalFinalize()
    {
        val craftStack = NotebookHandler.AS_NMS_COPY.invoke(null, notebook)
        val compound = NotebookHandler.TAG_COMPOUND.newInstance()

        NotebookHandler.TAG_COMPOUND_SET_STRING.invoke(compound, "title", internalTitle)
        NotebookHandler.TAG_COMPOUND_SET_STRING.invoke(compound, "author", "Scala")

        val tagList = NotebookHandler.TAG_LIST.newInstance()
        val joined = internalMainPageDescription.joinToString("\n")
        val tagString = NotebookHandler.TAG_STRING_CONSTRUCTOR.newInstance(joined)

        NotebookHandler.TAG_LIST_ADD.invoke(tagList, tagString)

        NotebookHandler.TAG_COMPOUND_SET.invoke(compound, "pages", tagList)
        NotebookHandler.NMS_ITEM_STACK_TAG_FIELD.set(craftStack, compound)

        notebook = NotebookHandler.AS_BUKKIT_COPY.invoke(null, craftStack) as ItemStack
        hasFinalized = true
    }

    fun open(player: Player)
    {
        if (!hasFinalized) {
            try {
                internalFinalize()
            } catch (exception: Exception) {
                exception.printStackTrace()
                player.sendMessage("${CC.RED}Sorry, we couldn't open this notebook.")
                return
            }
        }

        val buf = Unpooled.buffer(256)
        buf.setByte(0, 0)
        buf.writerIndex(1)

        val packet = NotebookHandler.CUSTOM_PAYLOAD_CONSTRUCTOR.newInstance(
            "MC|BOpen", NotebookHandler.DATA_SERIALIZER_CONSTRUCTOR.newInstance(buf)
        )

        val previous = player.inventory.heldItemSlot
        val previousItem = player.inventory.getItem(previous)

        player.inventory.setItem(previous, notebook)

        MinecraftProtocol.send(player, packet)

        player.inventory.setItem(previous, previousItem)
    }
}
