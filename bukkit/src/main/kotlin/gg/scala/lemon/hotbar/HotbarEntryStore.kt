package gg.scala.lemon.hotbar

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry

/**
 * @author GrowlyX
 * @since 7/5/2023
 */
object HotbarEntryStore : MutableMap<String, HotbarPresetEntry> by mutableMapOf()
