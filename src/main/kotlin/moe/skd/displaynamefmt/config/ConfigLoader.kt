package moe.skd.displaynamefmt.config

import moe.skd.displaynamefmt.condition.ConditionParser
import moe.skd.displaynamefmt.condition.LogicalType
import moe.skd.displaynamefmt.condition.NamedCondition
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.NodeTuple
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.SequenceNode
import java.nio.file.Path
import java.util.Locale
import java.util.logging.Logger

internal object ConfigLoader {
    fun load(path: Path, template: String, logger: Logger): DisplayNameConfig {
        val root = YamlDocument.prepare(path, template, logger)
        val displayName = root.mapping("display-name")
        val format = displayName.scalar("format")
        require(format.isNotBlank()) { "display-name.format cannot be blank" }
        val refreshInterval = displayName.scalar("refresh-interval-ticks").toLongOrNull()
        require(refreshInterval != null && refreshInterval >= 1) {
            "display-name.refresh-interval-ticks must be a whole number of at least 1"
        }

        return DisplayNameConfig(
            format = format,
            refreshIntervalTicks = refreshInterval,
            conditions = conditions(root.mapping("conditions")),
        )
    }

    private fun conditions(root: MappingNode): Map<String, NamedCondition> {
        val conditions = linkedMapOf<String, NamedCondition>()
        for (tuple in root.value) {
            val configuredName = tuple.key()
            require(CONDITION_NAME.matches(configuredName)) {
                "condition name '$configuredName' must match ${CONDITION_NAME.pattern}"
            }
            val name = configuredName.lowercase(Locale.ROOT)
            require(name !in conditions) { "duplicate condition name: $configuredName" }
            val node = tuple.valueNode as? MappingNode
                ?: error("conditions.$configuredName must be a section")
            val expressions = node.stringList("conditions")
            require(expressions.isNotEmpty()) { "conditions.$configuredName.conditions cannot be empty" }
            conditions[name] = NamedCondition(
                type = node.optionalScalar("type")
                    ?.uppercase(Locale.ROOT)
                    ?.let { raw ->
                        runCatching { LogicalType.valueOf(raw) }.getOrElse {
                            error("conditions.$configuredName.type must be AND or OR")
                        }
                    }
                    ?: LogicalType.AND,
                expressions = expressions.mapIndexed { index, expression ->
                    runCatching { ConditionParser.parse(expression) }.getOrElse { cause ->
                        throw IllegalArgumentException(
                            "conditions.$configuredName.conditions[$index]: ${cause.message}",
                            cause,
                        )
                    }
                },
                trueValue = node.optionalScalar("true") ?: "true",
                falseValue = node.optionalScalar("false") ?: "false",
            )
        }
        return conditions
    }

    private fun MappingNode.mapping(key: String): MappingNode {
        val node = child(key) ?: error("missing config section: $key")
        return node as? MappingNode ?: error("$key must be a section")
    }

    private fun MappingNode.scalar(key: String): String =
        optionalScalar(key) ?: error("missing config key: $key")

    private fun MappingNode.optionalScalar(key: String): String? {
        val node = child(key) ?: return null
        return (node as? ScalarNode)?.value ?: error("$key must be a scalar value")
    }

    private fun MappingNode.stringList(key: String): List<String> {
        val node = child(key) ?: return emptyList()
        return when (node) {
            is ScalarNode -> listOf(node.value)
            is SequenceNode -> node.value.mapIndexed { index, child ->
                (child as? ScalarNode)?.value ?: error("$key[$index] must be a scalar value")
            }
            else -> error("$key must be a scalar or list")
        }
    }

    private fun MappingNode.child(key: String): Node? =
        value.firstOrNull { it.key() == key }?.valueNode

    private fun NodeTuple.key(): String =
        (keyNode as? ScalarNode)?.value ?: error("config keys must be scalar values")

    private val CONDITION_NAME = Regex("[a-z0-9][a-z0-9_-]*")
}
