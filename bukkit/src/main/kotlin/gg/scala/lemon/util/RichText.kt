package gg.scala.lemon.util

import net.evilblock.cubed.util.Color
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import java.util.Locale

private val miniMessage = MiniMessage.miniMessage()
private val legacyRichTextSerializer = LegacyComponentSerializer.legacySection().toBuilder()
    .useUnusualXRepeatedCharacterHexFormat()
    .extractUrls()
    .build()

fun String.formatRichText(): String
{
    val enumColor = kotlin.runCatching {
        ChatColor.valueOf(trim().uppercase(Locale.getDefault())).toString()
    }.getOrNull()
    if (enumColor != null) {
        return enumColor
    }

    val translated = Color.translate(this)
    if ('<' !in this || '>' !in this) {
        return translated
    }

    return runCatching {
        legacyRichTextSerializer.serialize(miniMessage.deserialize(this))
    }.getOrElse {
        translated
    }
}
