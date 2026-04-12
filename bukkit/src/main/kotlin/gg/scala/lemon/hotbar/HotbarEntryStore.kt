package gg.scala.lemon.hotbar

import gg.scala.lemon.hotbar.entry.HotbarPresetEntry
import java.util.*

object HotbarEntryStore : MutableMap<String, HotbarPresetEntry> by mutableMapOf()
