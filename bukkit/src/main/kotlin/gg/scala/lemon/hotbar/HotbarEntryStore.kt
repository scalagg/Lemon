package gg.scala.lemon.hotbar

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import java.util.*

/**
 * @author GrowlyX
 * @since 7/5/2023
 */
object HotbarEntryStore : MutableMap<String, HotbarPresetEntry> by mutableMapOf()
