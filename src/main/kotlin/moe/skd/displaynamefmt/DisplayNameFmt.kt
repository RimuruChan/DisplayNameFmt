package moe.skd.displaynamefmt

import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import moe.skd.displaynamefmt.config.ConfigLoader
import java.nio.charset.StandardCharsets

class DisplayNameFmt : JavaPlugin() {
    @Volatile
    private lateinit var renderer: DisplayNameRenderer
    private var refreshTask: BukkitTask? = null
    private var expansion: DisplayNameExpansion? = null

    override fun onEnable() {
        val config = runCatching(::loadConfig).getOrElse { cause ->
            logger.severe("Could not load config.yml: ${cause.message}")
            server.pluginManager.disablePlugin(this)
            return
        }
        applyConfig(config)

        val papiExpansion = DisplayNameExpansion(this, implementationVersion)
        if (!papiExpansion.register()) {
            logger.severe("Could not register the DisplayNameFmt PlaceholderAPI expansion.")
            server.pluginManager.disablePlugin(this)
            return
        }
        expansion = papiExpansion
        server.pluginManager.registerEvents(PlayerListener(this), this)
        refreshAll()
    }

    override fun onDisable() {
        refreshTask?.cancel()
        refreshTask = null
        expansion?.unregister()
        expansion = null
        for (player in server.onlinePlayers) {
            player.displayName(Component.text(player.name))
        }
    }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (args.size != 1 || !args[0].equals("reload", ignoreCase = true)) {
            sender.sendPlainMessage("Usage: /$label reload")
            return true
        }

        val config = runCatching(::loadConfig).getOrElse { cause ->
            sender.sendPlainMessage("DisplayNameFmt reload failed: ${cause.message}")
            return true
        }
        applyConfig(config)
        refreshAll()
        sender.sendPlainMessage("DisplayNameFmt configuration reloaded.")
        return true
    }

    internal fun refresh(player: Player) {
        val displayName = renderer.render(player)
        if (player.displayName() != displayName) {
            player.displayName(displayName)
        }
    }

    internal fun renderCondition(name: String, player: Player): String? =
        renderer.renderCondition(name, player)

    internal fun renderPrefix(player: Player): String =
        renderer.renderPrefix(player)

    internal fun renderSuffix(player: Player): String =
        renderer.renderSuffix(player)

    private fun refreshAll() {
        server.onlinePlayers.forEach(::refresh)
    }

    private fun applyConfig(config: moe.skd.displaynamefmt.config.DisplayNameConfig) {
        renderer = DisplayNameRenderer(config, logger)
        refreshTask?.cancel()
        refreshTask = server.scheduler.runTaskTimer(
            this,
            Runnable(::refreshAll),
            config.refreshIntervalTicks,
            config.refreshIntervalTicks,
        )
    }

    private fun loadConfig(): moe.skd.displaynamefmt.config.DisplayNameConfig =
        ConfigLoader.load(
            dataFolder.toPath().resolve("config.yml"),
            requireNotNull(getResource("config.yml")) { "Bundled config.yml is missing" }
                .bufferedReader(StandardCharsets.UTF_8)
                .use { it.readText() },
            logger,
        )

    private val implementationVersion: String
        get() = DisplayNameFmt::class.java.`package`.implementationVersion ?: "development"
}
