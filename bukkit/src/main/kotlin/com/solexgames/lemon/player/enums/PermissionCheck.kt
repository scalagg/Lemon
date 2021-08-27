package com.solexgames.lemon.player.enums

/**
 * @author GrowlyX
 * @since 8/27/2021
 */
enum class PermissionCheck {

    COMPOUNDED, // Much more server intensive, looks through all rank inheritances for a match
    PLAYER, // Light-weight, checks if a permission exists in the player's attachment
    BOTH // Checks for a match in both their attachment & compounded rank permissions

}
