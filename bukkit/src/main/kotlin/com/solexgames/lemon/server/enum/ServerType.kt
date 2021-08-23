package com.solexgames.lemon.server.enum

import org.bukkit.Material

enum class ServerType(typeString: String, icon: Material, durability: Short = 0) {

    PRACTICE("Practice", Material.SPLASH_POTION, 16421),
    KITPVP("KitPvP", Material.IRON_SWORD),
    HARDCORE_FACTIONS("HCF", Material.MAGMA_CREAM),
    KITMAP("KitMap", Material.DIAMOND_AXE),
    SKYWARS("SkyWars", Material.EYE_OF_ENDER),
    BEDWARS("BedWars", Material.BED),
    MEETUP("UHC Meetup", Material.FISHING_ROD),
    UHC_GAMES("UHC Games", Material.CHEST),
    UHC("UHC", Material.GOLDEN_APPLE),
    HUB("Lobby", Material.COMPASS),
    EVENT("Event", Material.NETHER_STAR),
    POTSG("PotSG", Material.DIAMOND_SWORD),
    UNDEFINED("Undefined", Material.REDSTONE);

}