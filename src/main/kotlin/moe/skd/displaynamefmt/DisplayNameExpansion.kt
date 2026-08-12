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
        return when {
            params.equals(PREFIX, ignoreCase = true) -> plugin.renderPrefix(player)
            params.equals(SUFFIX, ignoreCase = true) -> plugin.renderSuffix(player)
            params.startsWith(CONDITION_PREFIX, ignoreCase = true) ->
                plugin.renderCondition(params.substring(CONDITION_PREFIX.length), player)
            else -> null
        }
    }

    private companion object {
        const val PREFIX = "prefix"
        const val SUFFIX = "suffix"
        const val CONDITION_PREFIX = "condition_"
    }
}
