package gg.scala.lemon.notebook

import io.netty.buffer.ByteBuf
import net.evilblock.cubed.util.Reflection
import net.evilblock.cubed.util.nms.MinecraftReflection
import org.bukkit.inventory.ItemStack

/**
 * @author GrowlyX
 * @since 10/16/2021
 */
object NotebookHandler
{
    @JvmStatic
    val CUSTOM_PAYLOAD = MinecraftReflection.getNMSClass("PacketPlayOutCustomPayload")!!

    @JvmStatic
    val DATA_SERIALIZER = MinecraftReflection.getNMSClass("PacketDataSerializer")!!

    @JvmStatic
    val CUSTOM_PAYLOAD_CONSTRUCTOR = Reflection.getConstructor(
        CUSTOM_PAYLOAD, String::class.java, DATA_SERIALIZER
    )!!

    @JvmStatic
    val DATA_SERIALIZER_CONSTRUCTOR = Reflection.getConstructor(
        DATA_SERIALIZER, ByteBuf::class.java
    )!!

    @JvmStatic
    val ITEM_STACK = MinecraftReflection.getNMSClass("ItemStack")!!

    @JvmStatic
    val TAG_COMPOUND = MinecraftReflection.getNMSClass("NBTTagCompound")!!

    @JvmStatic
    val NBT_BASE = MinecraftReflection.getNMSClass("NBTBase")!!

    @JvmStatic
    val TAG_LIST = MinecraftReflection.getNMSClass("NBTTagList")!!

    @JvmStatic
    val TAG_LIST_ADD = Reflection.getDeclaredMethod(TAG_LIST, "add", NBT_BASE)!!

    @JvmStatic
    val TAG_STRING = MinecraftReflection.getNMSClass("NBTTagString")!!

    @JvmStatic
    val TAG_STRING_CONSTRUCTOR = Reflection.getConstructor(
        TAG_STRING, String::class.java
    )!!

    @JvmStatic
    val TAG_COMPOUND_SET_STRING = Reflection.getDeclaredMethod(
        TAG_COMPOUND, "setString", String::class.java, String::class.java
    )!!

    @JvmStatic
    val TAG_COMPOUND_SET = Reflection.getDeclaredMethod(
        TAG_COMPOUND, "set", String::class.java, NBT_BASE
    )!!

    @JvmStatic
    val CRAFT_ITEM_STACK = MinecraftReflection.getCraftBukkitClass("inventory.CraftItemStack")!!

    @JvmStatic
    val NMS_ITEM_STACK = MinecraftReflection.getNMSClass("ItemStack")!!

    @JvmStatic
    val NMS_ITEM_STACK_TAG_FIELD = Reflection.getDeclaredField(NMS_ITEM_STACK, "tag")!!

    @JvmStatic
    val AS_NMS_COPY = Reflection.getDeclaredMethod(CRAFT_ITEM_STACK, "asNMSCopy", ItemStack::class.java)!!

    @JvmStatic
    val AS_BUKKIT_COPY = Reflection.getDeclaredMethod(CRAFT_ITEM_STACK, "asBukkitCopy", ITEM_STACK)!!

    init
    {
        NMS_ITEM_STACK_TAG_FIELD.isAccessible = true
    }

    /**
     * Creates a new instance of [WrappedNotebook]
     *
     * @return WrappedNotebook a new notebook.
     */
    fun createNotebook(): WrappedNotebook
    {
        return WrappedNotebook()
    }
}
