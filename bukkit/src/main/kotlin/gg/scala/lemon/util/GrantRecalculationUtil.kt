package gg.scala.lemon.util

import gg.scala.lemon.player.grant.Grant

object GrantRecalculationUtil {

    /**
     * Retrieves the prominent grant out
     * of a mutable list with grants
     */
    @JvmStatic
    fun getProminentGrant(grants: List<Grant>): Grant? {
        return grants
            .sortedByDescending { it.addedAt }
            .sortedByDescending { it.getRank().weight }
            .firstOrNull { !it.isRemoved && !it.hasExpired && it.getRank().visible && it.isApplicable() }
    }

    @JvmStatic
    fun getPermissionGrants(grants: List<Grant>): List<Grant> {
        return grants
            .sortedByDescending { it.addedAt }
            .filter { !it.isRemoved && !it.hasExpired && it.isApplicable() }
    }
}
