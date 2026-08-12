package moe.skd.displaynamefmt

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

internal object TextUtil {
    private val ampersandCode = Regex("&(?=#[0-9a-fA-F]{6}|[0-9a-fA-Fk-oK-OrR])")
    private val serializer = LegacyComponentSerializer.builder()
        .character('§')
        .hexCharacter('#')
        .hexColors()
        .build()

    fun deserialize(value: String): Component =
        serializer.deserialize(ampersandCode.replace(value, "§"))
}
