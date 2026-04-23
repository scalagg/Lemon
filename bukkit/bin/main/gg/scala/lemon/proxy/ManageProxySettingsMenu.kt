package gg.scala.lemon.proxy

import com.cryptomorin.xseries.XMaterial
import gg.scala.commons.configurable.*
import gg.scala.lemon.handler.RankHandler
import net.evilblock.cubed.menu.Button
import net.evilblock.cubed.menu.Menu
import org.bukkit.entity.Player

class ManageProxySettingsMenu : Menu("Managing proxy...")
{
    init
    {
        placeholder = true
    }

    override fun getButtons(player: Player) = mapOf(
        10 to editBoolean(ProxySettingsDataSync,
            title = "Show Player Count While Whitelisted",
            material = XMaterial.CLOCK,
            getter = ProxySettings::showPlayerCountWhileWhitelisted,
            setter = ProxySettings::showPlayerCountWhileWhitelisted::set
        ),

        11 to editBoolean(ProxySettingsDataSync,
            title = "Auto Register From Server Sync Discovery",
            material = XMaterial.EMERALD,
            getter = ProxySettings::autoRegisterFromServerSyncDiscovery,
            setter = ProxySettings::autoRegisterFromServerSyncDiscovery::set
        ),

        12 to editBoolean(ProxySettingsDataSync,
            title = "Ensure Token Whitelisted",
            material = XMaterial.BARRIER,
            getter = ProxySettings::ensureTokenWhitelisted,
            setter = ProxySettings::ensureTokenWhitelisted::set
        ),

        13 to editInt(ProxySettingsDataSync,
            title = "Token Whitelist Highest Weight",
            material = XMaterial.ANVIL,
            getter = ProxySettings::tokenWhitelistHighestWeightBeforeNotEffective,
            setter = ProxySettings::tokenWhitelistHighestWeightBeforeNotEffective::set
        ),

        14 to editString(ProxySettingsDataSync,
            title = "Server Brand",
            material = XMaterial.NAME_TAG,
            getter = ProxySettings::serverBrand,
            setter = ProxySettings::serverBrand::set
        ),

        15 to editBoolean(ProxySettingsDataSync,
            title = "Maintenance Mode",
            material = XMaterial.REDSTONE_TORCH,
            getter = { maintenance.enabled },
            setter = { maintenance.enabled = it }
        ),

        16 to editListSelector(ProxySettingsDataSync,
            title = "Maintenance Mode Minimum Rank",
            material = XMaterial.REDSTONE_TORCH,
            getter = { maintenance.rank ?: RankHandler.getDefaultRank().uuid },
            values = { RankHandler.ranks.keys.toList() },
            format = { RankHandler.findRank(this)?.getColoredName() ?: "???" },
            setter = { maintenance.rank = it }
        ),

        19 to editStringMap(ProxySettingsDataSync,
            title = "Preferred Hub Redirects",
            material = XMaterial.COMPASS,
            getter = ProxySettings::preferHubServerRedirects,
            setter = { preferHubServerRedirects = it.toMutableMap() }
        ),

        20 to editStringList(ProxySettingsDataSync,
            title = "Normal Presence",
            material = XMaterial.BOOK,
            getter = { presence.normalPresence.split("\n") },
            setter = { presence.normalPresence = it.joinToString("\n") }
        ),

        21 to editStringList(ProxySettingsDataSync,
            title = "Allowed Versions",
            material = XMaterial.GOLD_INGOT,
            getter = { allowedVersions.map { it.toString() } },
            setter = { allowedVersions = it.mapNotNull { number -> number.toIntOrNull() }.toMutableList() }
        ),

        22 to editBoolean(ProxySettingsDataSync,
            title = "Restrict Allowed Versions",
            material = XMaterial.ENCHANTED_BOOK,
            getter = ProxySettings::shouldRestrictAllowedVersions,
            setter = ProxySettings::shouldRestrictAllowedVersions::set
        ),

        23 to editStringList(ProxySettingsDataSync,
            title = "Listening Groups",
            material = XMaterial.BOOK,
            getter = ProxySettings::listeningGroups,
            setter = { listeningGroups = it.toMutableList() }
        ),

        24 to editStringList(ProxySettingsDataSync,
            title = "Listening Exemptions",
            material = XMaterial.BOOK,
            getter = ProxySettings::listeningExemptions,
            setter = { listeningExemptions = it.toMutableList() }
        ),

        25 to editString(ProxySettingsDataSync,
            title = "TOTP Issuer",
            material = XMaterial.PLAYER_HEAD,
            getter = ProxySettings::totpIssuer,
            setter = { totpIssuer = it }
        ),

        28 to editStringList(ProxySettingsDataSync,
            title = "Maintenance Presence",
            material = XMaterial.BOOK,
            getter = { presence.whitelistedPresence.split("\n") },
            setter = { presence.whitelistedPresence = it.joinToString("\n") }
        ),

        29 to editBoolean(ProxySettingsDataSync,
            title = "Should Center Presence",
            material = XMaterial.BOOK,
            getter = { presence.shouldCenterPresence },
            setter = { presence.shouldCenterPresence = it }
        ),
    )

    override fun size(buttons: Map<Int, Button>) = 45
}
