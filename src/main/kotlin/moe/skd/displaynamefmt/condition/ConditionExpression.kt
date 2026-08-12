package moe.skd.displaynamefmt.condition

internal data class EvaluationContext(
    val resolve: (String) -> String,
    val hasPermission: (String) -> Boolean,
)

internal sealed interface ConditionExpression {
    fun evaluate(context: EvaluationContext): Boolean
}

internal data class LogicalExpression(
    val type: LogicalType,
    val expressions: List<ConditionExpression>,
) : ConditionExpression {
    override fun evaluate(context: EvaluationContext): Boolean = when (type) {
        LogicalType.AND -> expressions.all { it.evaluate(context) }
        LogicalType.OR -> expressions.any { it.evaluate(context) }
    }
}

internal data class PermissionExpression(
    val permission: String,
    val negated: Boolean,
) : ConditionExpression {
    override fun evaluate(context: EvaluationContext): Boolean {
        val result = context.hasPermission(context.resolve(permission).trim())
        return if (negated) !result else result
    }
}

internal data class ComparisonExpression(
    val left: String,
    val operator: ComparisonOperator,
    val right: String,
) : ConditionExpression {
    override fun evaluate(context: EvaluationContext): Boolean {
        val resolvedLeft = context.resolve(left)
        val resolvedRight = context.resolve(right)
        return operator.evaluate(resolvedLeft, resolvedRight)
    }
}

internal enum class LogicalType {
    AND,
    OR,
}

internal enum class ComparisonOperator(val token: String) {
    GREATER_OR_EQUAL(">=") {
        override fun evaluate(left: String, right: String) = compareNumbers(left, right) { a, b -> a >= b }
    },
    GREATER(">") {
        override fun evaluate(left: String, right: String) = compareNumbers(left, right) { a, b -> a > b }
    },
    LESS_OR_EQUAL("<=") {
        override fun evaluate(left: String, right: String) = compareNumbers(left, right) { a, b -> a <= b }
    },
    LESS("<") {
        override fun evaluate(left: String, right: String) = compareNumbers(left, right) { a, b -> a < b }
    },
    EQUALS("=") {
        override fun evaluate(left: String, right: String) = left == right
    },
    NOT_EQUALS("!=") {
        override fun evaluate(left: String, right: String) = left != right
    },
    CONTAINS("<-") {
        override fun evaluate(left: String, right: String) = right in left
    },
    NOT_CONTAINS("!<-") {
        override fun evaluate(left: String, right: String) = right !in left
    },
    STARTS_WITH("|-") {
        override fun evaluate(left: String, right: String) = left.startsWith(right)
    },
    NOT_STARTS_WITH("!|-") {
        override fun evaluate(left: String, right: String) = !left.startsWith(right)
    },
    ENDS_WITH("-|") {
        override fun evaluate(left: String, right: String) = left.endsWith(right)
    },
    NOT_ENDS_WITH("!-|") {
        override fun evaluate(left: String, right: String) = !left.endsWith(right)
    };

    abstract fun evaluate(left: String, right: String): Boolean

    protected fun compareNumbers(
        left: String,
        right: String,
        comparison: (Double, Double) -> Boolean,
    ): Boolean {
        val leftNumber = left.trim().toDoubleOrNull() ?: return false
        val rightNumber = right.trim().toDoubleOrNull() ?: return false
        return comparison(leftNumber, rightNumber)
    }
}

internal data class NamedCondition(
    val type: LogicalType,
    val expressions: List<ConditionExpression>,
    val trueValue: String,
    val falseValue: String,
) {
    fun evaluate(context: EvaluationContext): Boolean = when (type) {
        LogicalType.AND -> expressions.all { it.evaluate(context) }
        LogicalType.OR -> expressions.any { it.evaluate(context) }
    }
}
