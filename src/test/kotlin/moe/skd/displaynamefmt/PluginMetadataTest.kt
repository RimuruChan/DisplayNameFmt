package moe.skd.displaynamefmt

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PluginMetadataTest {
    @Test
    fun declaresTheLowestSupportedPaperApiVersion() {
        val pluginMetadata = requireNotNull(javaClass.getResource("/plugin.yml"))
            .readText(Charsets.UTF_8)

        assertTrue(
            pluginMetadata.lineSequence().any { it.trim() == "api-version: '1.20'" },
            "plugin.yml must keep api-version 1.20 so every supported Paper release can load it",
        )
    }
}
