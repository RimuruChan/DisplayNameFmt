package moe.skd.displaynamefmt

import me.clip.placeholderapi.PlaceholderAPI
import moe.skd.displaynamefmt.condition.ConditionEngine
import moe.skd.displaynamefmt.config.DisplayNameConfig
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

internal class DisplayNameRenderer(
    private val config: DisplayNameConfig,
    private val logger: Logger,
) {
    private val conditions = ConditionEngine(config.conditions, logger)
    private val fragmentStack = ThreadLocal.withInitial<ArrayDeque<String>> { ArrayDeque() }
    private val reportedCycles = ConcurrentHashMap.newKeySet<String>()

    fun render(player: Player): Component =
        TextUtil.deserialize(PlaceholderAPI.setPlaceholders(player, config.format))

    fun renderCondition(name: String, player: Player): String? =
        conditions.render(name, player)

    fun renderPrefix(player: Player): String =
        renderFragment("prefix", config.prefix, player)

    fun renderSuffix(player: Player): String =
        renderFragment("suffix", config.suffix, player)

    private fun renderFragment(name: String, value: String, player: Player): String {
        val stack = fragmentStack.get()
        if (name in stack) {
            val cycle = (stack + name).joinToString(" -> ")
            if (reportedCycles.add(cycle)) {
                logger.warning("Cyclic display-name fragment: $cycle")
            }
            return ""
        }

        stack.addLast(name)
        return try {
            PlaceholderAPI.setPlaceholders(player, value)
        } finally {
            stack.removeLast()
            if (stack.isEmpty()) {
                fragmentStack.remove()
            }
        }
    }
}
