package com.solexgames.lemon.util

import com.solexgames.lemon.player.grant.Grant
import java.util.stream.Collectors

object GrantRecalculationUtil {

    /**
     * Retrieves the prominent grant out
     * of a mutable list with grants
     */
    @JvmStatic
    fun getProminentGrant(grants: List<Grant>): Grant? {
        return grants
            .sortedByDescending { it.addedAt }
            .firstOrNull { it.removedBy == null && !it.hasExpired() && !it.getRank().hidden && it.isApplicable() }
    }

    @JvmStatic
    fun getPermissionGrants(grants: List<Grant>): List<Grant> {
        return grants
            .sortedByDescending { it.addedAt }
            .filter { it.removedBy == null && !it.hasExpired() && it.isApplicable() }
    }
}
