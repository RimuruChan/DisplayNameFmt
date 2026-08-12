package moe.skd.displaynamefmt.condition

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConditionParserTest {
    @Test
    fun evaluatesNumberComparisons() {
        assertTrue(evaluate("%ping%>=100", "%ping%" to "100"))
        assertTrue(evaluate("%ping%>100", "%ping%" to "101"))
        assertTrue(evaluate("%ping%<=100", "%ping%" to "100"))
        assertTrue(evaluate("%ping%<100", "%ping%" to "99.5"))
        assertFalse(evaluate("%ping%>100", "%ping%" to "not-a-number"))
    }

    @Test
    fun evaluatesTextOperationsExactly() {
        assertTrue(evaluate("%world%=world", "%world%" to "world"))
        assertTrue(evaluate("%world%!=nether", "%world%" to "world"))
        assertTrue(evaluate("%world%<-lobby-", "%world%" to "main-lobby-1"))
        assertTrue(evaluate("%world%!<-lobby-", "%world%" to "world"))
        assertTrue(evaluate("%world%|-lobby-", "%world%" to "lobby-1"))
        assertTrue(evaluate("%world%!|-lobby-", "%world%" to "main-lobby"))
        assertTrue(evaluate("%world%-|nether", "%world%" to "world_nether"))
        assertTrue(evaluate("%world%!-|nether", "%world%" to "world"))
        assertFalse(evaluate("%world%=world", "%world%" to "WORLD"))
    }

    @Test
    fun supportsEmptyEqualityChecks() {
        assertTrue(evaluate("%optional%=", "%optional%" to ""))
        assertTrue(evaluate("%optional%!=", "%optional%" to "value"))
    }

    @Test
    fun evaluatesPermissionRequirements() {
        assertTrue(evaluate("permission:group.admin", permissions = setOf("group.admin")))
        assertTrue(evaluate("!permission:group.admin"))
        assertFalse(evaluate("!permission:group.admin", permissions = setOf("group.admin")))
    }

    @Test
    fun supportsTabShortAndOrFormats() {
        assertTrue(
            evaluate(
                "%world%=world;%ping%<100",
                "%world%" to "world",
                "%ping%" to "50",
            ),
        )
        assertTrue(
            evaluate(
                "%world%=world|%world%=lobby",
                "%world%" to "lobby",
            ),
        )
        assertFalse(
            evaluate(
                "%world%=world;%ping%<100",
                "%world%" to "world",
                "%ping%" to "150",
            ),
        )
    }

    @Test
    fun doesNotConfuseTextOperatorsWithOrSeparators() {
        assertTrue(evaluate("%world%|-lobby-", "%world%" to "lobby-1"))
        assertTrue(evaluate("%world%-|nether", "%world%" to "world_nether"))
    }

    @Test
    fun rejectsMixedAndOrAndMalformedExpressions() {
        assertFailsWith<IllegalArgumentException> {
            ConditionParser.parse("%world%=world;%ping%<100|permission:test")
        }
        assertFailsWith<IllegalStateException> {
            ConditionParser.parse("%world%")
        }
        assertFailsWith<IllegalArgumentException> {
            ConditionParser.parse("permission:")
        }
    }

    private fun evaluate(
        expression: String,
        vararg values: Pair<String, String>,
        permissions: Set<String> = emptySet(),
    ): Boolean {
        val replacements = values.toMap()
        return ConditionParser.parse(expression).evaluate(
            EvaluationContext(
                resolve = { replacements[it] ?: it },
                hasPermission = { it in permissions },
            ),
        )
    }
}
