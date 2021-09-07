package com.solexgames.lemon.util.quickaccess

import com.solexgames.lemon.util.other.Cooldown

/**
 * @author GrowlyX
 * @since 9/6/2021
 */
fun remaining(cooldown: Cooldown): String {
    return String.format("%.0f", (cooldown.getRemaining() / 1000).toFloat())
}
