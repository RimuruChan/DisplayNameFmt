package moe.skd.displaynamefmt.condition

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.entity.Player
import java.util.ArrayDeque
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

internal class ConditionEngine(
    private val conditions: Map<String, NamedCondition>,
    private val logger: Logger,
) {
    private val evaluationStack = ThreadLocal.withInitial<ArrayDeque<String>> { ArrayDeque() }
    private val reportedCycles = ConcurrentHashMap.newKeySet<String>()

    fun render(name: String, player: Player): String? {
        val normalizedName = name.lowercase(Locale.ROOT)
        val condition = conditions[normalizedName] ?: return null
        val stack = evaluationStack.get()
        if (normalizedName in stack) {
            val cycle = (stack + normalizedName).joinToString(" -> ")
            if (reportedCycles.add(cycle)) {
                logger.warning("Cyclic display-name condition: $cycle")
            }
            return ""
        }

        stack.addLast(normalizedName)
        return try {
            val context = EvaluationContext(
                resolve = { PlaceholderAPI.setPlaceholders(player, it) },
                hasPermission = player::hasPermission,
            )
            val output = if (condition.evaluate(context)) condition.trueValue else condition.falseValue
            PlaceholderAPI.setPlaceholders(player, output)
        } finally {
            stack.removeLast()
            if (stack.isEmpty()) {
                evaluationStack.remove()
            }
        }
    }
}
