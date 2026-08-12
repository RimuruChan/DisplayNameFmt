package moe.skd.displaynamefmt

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

internal class DisplayNameExpansion(
    private val plugin: DisplayNameFmt,
    private val pluginVersion: String,
) : PlaceholderExpansion() {
    override fun getIdentifier(): String = "displaynamefmt"

    override fun getAuthor(): String = "RimuruChan"

    override fun getVersion(): String = pluginVersion

    override fun persist(): Boolean = true

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        player ?: return null
        if (!params.startsWith(CONDITION_PREFIX, ignoreCase = true)) return null
        return plugin.renderCondition(params.substring(CONDITION_PREFIX.length), player)
    }

    private companion object {
        const val CONDITION_PREFIX = "condition_"
    }
}
