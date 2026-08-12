package moe.skd.displaynamefmt.config

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.NodeTuple
import org.yaml.snakeyaml.nodes.ScalarNode
import org.yaml.snakeyaml.nodes.Tag
import org.yaml.snakeyaml.representer.Representer
import java.io.StringReader
import java.io.StringWriter
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.logging.Logger

internal object YamlDocument {
    fun prepare(path: Path, template: String, logger: Logger): MappingNode {
        path.parent?.let(Files::createDirectories)
        if (Files.notExists(path)) {
            Files.writeString(path, template)
        }

        val yaml = yaml()
        val root = compose(yaml, Files.readString(path), path.fileName.toString())
        val migrated = migrate(root)
        val defaults = compose(yaml, template, "bundled config.yml")
        val added = mutableListOf<String>()
        backfill(root, defaults, "", setOf(VERSION_KEY), added)

        if (migrated || added.isNotEmpty()) {
            write(path, render(yaml, root))
        }
        if (migrated) {
            logger.info("Migrated config.yml to config-version $CONFIG_VERSION")
        }
        if (added.isNotEmpty()) {
            logger.info("Added ${added.size} missing config key(s): ${added.joinToString(", ")}")
        }
        return root
    }

    private fun migrate(root: MappingNode): Boolean {
        var version = versionOf(root)
        require(version <= CONFIG_VERSION) {
            "config.yml was written by a newer DisplayNameFmt " +
                "($VERSION_KEY $version, this build reads $CONFIG_VERSION)"
        }
        var changed = false
        while (version < CONFIG_VERSION) {
            when (version) {
                0 -> migrateV0(root)
                else -> error("No migration exists from config-version $version")
            }
            version++
            setVersion(root, version)
            changed = true
        }
        return changed
    }

    /** Moves the pre-version root keys into the display-name section. */
    private fun migrateV0(root: MappingNode) {
        val tuples = root.value.toMutableList()
        val legacyFormat = tuples.removeByKey("format")
        val legacyRefresh = tuples.removeByKey("refresh-interval-ticks")
        if (legacyFormat == null && legacyRefresh == null) {
            root.value = tuples
            return
        }

        var displayTuple = tuples.firstByKey("display-name")
        val display = if (displayTuple == null) {
            MappingNode(Tag.MAP, mutableListOf(), DumperOptions.FlowStyle.BLOCK).also { node ->
                displayTuple = NodeTuple(scalar("display-name"), node)
                tuples += checkNotNull(displayTuple)
            }
        } else {
            require(displayTuple.valueNode is MappingNode) { "display-name must be a section" }
            displayTuple.valueNode as MappingNode
        }

        val displayValues = display.value.toMutableList()
        if (legacyFormat != null && displayValues.firstByKey("format") == null) {
            displayValues += legacyFormat
        }
        if (legacyRefresh != null && displayValues.firstByKey("refresh-interval-ticks") == null) {
            displayValues += legacyRefresh
        }
        display.value = displayValues
        root.value = tuples
    }

    private fun versionOf(root: MappingNode): Int {
        val tuple = root.value.firstByKey(VERSION_KEY) ?: return 0
        val raw = (tuple.valueNode as? ScalarNode)?.value
        val version = raw?.toIntOrNull()
        require(version != null && version >= 0) { "$VERSION_KEY must be a non-negative whole number" }
        return version
    }

    private fun setVersion(root: MappingNode, version: Int) {
        val tuples = root.value.toMutableList()
        val index = tuples.indexOfFirst { keyOf(it) == VERSION_KEY }
        val replacement = NodeTuple(scalar(VERSION_KEY), scalar(version.toString(), Tag.INT))
        if (index < 0) tuples.add(0, replacement) else tuples[index] = replacement
        root.value = tuples
    }

    private fun backfill(
        target: MappingNode,
        defaults: MappingNode,
        prefix: String,
        excluded: Set<String>,
        added: MutableList<String>,
    ) {
        val tuples = target.value.toMutableList()
        val existing = tuples.associateBy(::keyOf)
        for (tuple in defaults.value) {
            val key = keyOf(tuple) ?: continue
            if (prefix.isEmpty() && key in excluded) continue
            val current = existing[key]
            if (current == null) {
                tuples += tuple
                added += prefix + key
                continue
            }
            val into = current.valueNode as? MappingNode ?: continue
            val from = tuple.valueNode as? MappingNode ?: continue
            backfill(into, from, "$prefix$key.", excluded, added)
        }
        target.value = tuples
    }

    private fun compose(yaml: Yaml, text: String, source: String): MappingNode =
        requireNotNull(yaml.compose(StringReader(text)) as? MappingNode) {
            "$source must contain a YAML mapping"
        }

    private fun render(yaml: Yaml, root: MappingNode): String =
        StringWriter().also { yaml.serialize(root, it) }.toString()
            .lineSequence()
            .joinToString("\n") { line -> if (line.isBlank()) "" else line }

    private fun write(path: Path, text: String) {
        val temporary = path.resolveSibling("${path.fileName}.tmp")
        Files.writeString(temporary, text)
        try {
            Files.move(
                temporary,
                path,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun yaml(): Yaml {
        val loaderOptions = LoaderOptions().apply { isProcessComments = true }
        val dumperOptions = DumperOptions().apply {
            isProcessComments = true
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            indent = 2
            indicatorIndent = 2
            indentWithIndicator = true
            isAllowUnicode = true
            splitLines = false
            width = Int.MAX_VALUE
        }
        return Yaml(
            SafeConstructor(loaderOptions),
            Representer(dumperOptions),
            dumperOptions,
            loaderOptions,
        )
    }

    private fun scalar(value: String, tag: Tag = Tag.STR) =
        ScalarNode(tag, value, null, null, DumperOptions.ScalarStyle.PLAIN)

    private fun MutableList<NodeTuple>.removeByKey(key: String): NodeTuple? {
        val index = indexOfFirst { keyOf(it) == key }
        return if (index < 0) null else removeAt(index)
    }

    private fun Iterable<NodeTuple>.firstByKey(key: String): NodeTuple? =
        firstOrNull { keyOf(it) == key }

    private fun keyOf(tuple: NodeTuple): String? = (tuple.keyNode as? ScalarNode)?.value

    const val CONFIG_VERSION = 1
    private const val VERSION_KEY = "config-version"
}
