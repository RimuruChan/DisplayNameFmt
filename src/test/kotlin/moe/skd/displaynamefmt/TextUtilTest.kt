package moe.skd.displaynamefmt

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TextUtilTest {
    @Test
    fun parsesAmpersandRgbColors() {
        val text = TextUtil.deserialize("&#12ab34Display") as TextComponent

        assertEquals("Display", text.content())
        assertEquals(TextColor.color(0x12AB34), text.color())
    }
}
