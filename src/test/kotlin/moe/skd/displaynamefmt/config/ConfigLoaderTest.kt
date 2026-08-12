package moe.skd.displaynamefmt.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import moe.skd.displaynamefmt.condition.EvaluationContext
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ConfigLoaderTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun createsTheBundledConfigWhenMissing() {
        val path = temporaryDirectory.resolve("config.yml")

        val config = ConfigLoader.load(path, template(), LOGGER)

        assertEquals("%player_name%", config.format)
        assertEquals(20, config.refreshIntervalTicks)
        assertTrue(config.conditions.isEmpty())
        assertTrue(Files.readString(path).contains("config-version: 1"))
    }

    @Test
    fun migratesRootDisplayKeysFromVersionZero() {
        val path = temporaryDirectory.resolve("config.yml")
        Files.writeString(
            path,
            """
            # Keep this operator comment.
            format: '%player_name% &#ff0000legacy'
            refresh-interval-ticks: 40
            conditions: {}
            """.trimIndent(),
        )

        val config = ConfigLoader.load(path, template(), LOGGER)
        val migrated = Files.readString(path)

        assertEquals("%player_name% &#ff0000legacy", config.format)
        assertEquals(40, config.refreshIntervalTicks)
        assertTrue(migrated.contains("config-version: 1"))
        assertTrue(migrated.contains("display-name:"))
        assertTrue(migrated.contains("# Keep this operator comment."))
        assertTrue(migrated.lineSequence().none { it.startsWith("format:") })
    }

    @Test
    fun backfillsMissingKeysWithoutReplacingConfiguredValues() {
        val path = temporaryDirectory.resolve("config.yml")
        Files.writeString(
            path,
            """
            config-version: 1
            display-name:
              format: '%player_name% custom'
            conditions: {}
            """.trimIndent(),
        )

        val config = ConfigLoader.load(path, template(), LOGGER)

        assertEquals("%player_name% custom", config.format)
        assertEquals(20, config.refreshIntervalTicks)
        assertTrue(config.conditions.isEmpty())
    }

    @Test
    fun parsesNamedConditionsAndDefaultsTheirOutputs() {
        val path = temporaryDirectory.resolve("config.yml")
        Files.writeString(
            path,
            """
            config-version: 1
            display-name:
              format: '%displaynamefmt_condition_fast%%player_name%'
              refresh-interval-ticks: 10
            conditions:
              fast:
                conditions:
                  - '%player_ping%<100'
            """.trimIndent(),
        )

        val condition = ConfigLoader.load(path, template(), LOGGER).conditions.getValue("fast")
        val context = EvaluationContext(
            resolve = { if (it == "%player_ping%") "50" else it },
            hasPermission = { false },
        )

        assertTrue(condition.evaluate(context))
        assertEquals("true", condition.trueValue)
        assertEquals("false", condition.falseValue)
    }

    @Test
    fun rejectsConfigsWrittenByANewerVersion() {
        val path = temporaryDirectory.resolve("config.yml")
        Files.writeString(path, "config-version: 999\n")

        val error = assertFailsWith<IllegalArgumentException> {
            ConfigLoader.load(path, template(), LOGGER)
        }

        assertTrue(error.message.orEmpty().contains("newer DisplayNameFmt"))
    }

    private fun template(): String = requireNotNull(javaClass.getResource("/config.yml"))
        .readText(Charsets.UTF_8)

    private companion object {
        val LOGGER: Logger = Logger.getLogger(ConfigLoaderTest::class.java.name)
    }
}
