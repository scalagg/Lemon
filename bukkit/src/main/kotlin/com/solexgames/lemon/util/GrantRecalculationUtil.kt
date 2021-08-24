package com.solexgames.lemon.util

import com.solexgames.lemon.player.grant.Grant
import java.util.stream.Collectors

object GrantRecalculationUtil {

    /**
     * Retrieves the prominent grant out
     * of a mutable list with grants
     */
    @JvmStatic
    fun getProminentGrant(grants: MutableList<Grant>): Grant {
        return grants.stream()
            .sorted(Comparator.comparingLong(Grant::addedAt).reversed())
            .collect(Collectors.toList()).stream()
            .filter { grant ->
                grant != null && !grant.removed && !grant.hasExpired() && !grant.getRank().hidden && grant.isApplicable()
            }.findFirst().orElse(null)
    }
}
