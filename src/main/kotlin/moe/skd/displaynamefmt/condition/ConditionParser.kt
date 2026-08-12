package moe.skd.displaynamefmt.condition

internal object ConditionParser {
    fun parse(input: String): ConditionExpression {
        val expression = input.trim()
        require(expression.isNotEmpty()) { "condition expression cannot be blank" }

        val andSeparators = expression.indices.filter { expression[it] == ';' }
        val orSeparators = expression.indices.filter { index ->
            expression[index] == '|' &&
                expression.getOrNull(index - 1) != '-' &&
                expression.getOrNull(index + 1) != '-'
        }
        require(andSeparators.isEmpty() || orSeparators.isEmpty()) {
            "cannot combine AND (;) and OR (|) in one expression: $input"
        }

        val separators = andSeparators.ifEmpty { orSeparators }
        if (separators.isNotEmpty()) {
            val parts = split(expression, separators)
            require(parts.all(String::isNotBlank)) { "condition contains an empty branch: $input" }
            return LogicalExpression(
                if (andSeparators.isNotEmpty()) LogicalType.AND else LogicalType.OR,
                parts.map(::parseAtomic),
            )
        }
        return parseAtomic(expression)
    }

    private fun parseAtomic(expression: String): ConditionExpression {
        if (expression.startsWith(NEGATED_PERMISSION_PREFIX)) {
            return PermissionExpression(
                permission = expression.removePrefix(NEGATED_PERMISSION_PREFIX).requireValue(expression),
                negated = true,
            )
        }
        if (expression.startsWith(PERMISSION_PREFIX)) {
            return PermissionExpression(
                permission = expression.removePrefix(PERMISSION_PREFIX).requireValue(expression),
                negated = false,
            )
        }

        for (operator in OPERATORS) {
            val index = expression.indexOf(operator.token)
            if (index <= 0) continue
            return ComparisonExpression(
                left = expression.substring(0, index),
                operator = operator,
                right = expression.substring(index + operator.token.length),
            )
        }
        error("unsupported condition expression: $expression")
    }

    private fun split(expression: String, separators: List<Int>): List<String> {
        val result = ArrayList<String>(separators.size + 1)
        var start = 0
        for (separator in separators) {
            result += expression.substring(start, separator)
            start = separator + 1
        }
        result += expression.substring(start)
        return result
    }

    private fun String.requireValue(expression: String): String =
        trim().also { require(it.isNotEmpty()) { "permission is missing in condition: $expression" } }

    private const val PERMISSION_PREFIX = "permission:"
    private const val NEGATED_PERMISSION_PREFIX = "!permission:"

    private val OPERATORS = listOf(
        ComparisonOperator.NOT_CONTAINS,
        ComparisonOperator.NOT_STARTS_WITH,
        ComparisonOperator.NOT_ENDS_WITH,
        ComparisonOperator.GREATER_OR_EQUAL,
        ComparisonOperator.LESS_OR_EQUAL,
        ComparisonOperator.NOT_EQUALS,
        ComparisonOperator.CONTAINS,
        ComparisonOperator.STARTS_WITH,
        ComparisonOperator.ENDS_WITH,
        ComparisonOperator.GREATER,
        ComparisonOperator.LESS,
        ComparisonOperator.EQUALS,
    )
}
