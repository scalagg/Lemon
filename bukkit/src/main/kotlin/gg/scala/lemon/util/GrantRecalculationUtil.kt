package gg.scala.lemon.util

import gg.scala.lemon.player.grant.Grant

object GrantRecalculationUtil {

    @JvmStatic
    fun getProminentGrant(grants: List<Grant>): Grant? {
        return grants
            .sortedByDescending { it.getRank().weight }
            .firstOrNull { it.isActive && it.getRank().visible && it.isApplicable() }
    }

    @JvmStatic
    fun getProminentSubGrant(prominent: Grant, grants: List<Grant>): Grant? {
        return grants
            .sortedByDescending { it.getRank().weight }
            .firstOrNull {
                it.uuid != prominent.uuid && it.isActive && it.isApplicable() && it.getRank().visible
            }
    }

    @JvmStatic
    fun getPermissionGrants(grants: List<Grant>): List<Grant> {
        return grants
            .sortedByDescending { it.addedAt }
            .filter { !it.isRemoved && !it.hasExpired && it.isApplicable() }
    }
}
