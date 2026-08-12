package moe.skd.displaynamefmt

import me.clip.placeholderapi.PlaceholderAPI
import moe.skd.displaynamefmt.condition.ConditionEngine
import moe.skd.displaynamefmt.config.DisplayNameConfig
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.logging.Logger

internal class DisplayNameRenderer(
    private val config: DisplayNameConfig,
    logger: Logger,
) {
    private val conditions = ConditionEngine(config.conditions, logger)

    fun render(player: Player): Component =
        TextUtil.deserialize(PlaceholderAPI.setPlaceholders(player, config.format))

    fun renderCondition(name: String, player: Player): String? =
        conditions.render(name, player)
}
