package com.solexgames.lemon.util.quickaccess

import net.evilblock.cubed.util.CC

/**
 * @author GrowlyX
 * @since 9/11/2021
 */
fun replaceEmpty(string: String): String {
    return string.ifBlank {
        "${CC.RED}None"
    }
}
