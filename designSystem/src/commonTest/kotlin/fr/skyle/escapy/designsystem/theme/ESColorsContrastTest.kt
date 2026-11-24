package fr.skyle.escapy.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

/** WCAG AA: 4.5:1 for a text, 3:1 for the boundary of a control and for an icon. */
class ESColorsContrastTest {

    private val colors = escapyColors

    @Test
    fun `the texts are readable on every surface they are drawn on`() {
        val surfaces = listOf(colors.background, colors.surface, colors.surfaceHigh, colors.surfaceHighest)
        surfaces.forEach { surface ->
            assertContrast(foreground = colors.textPrimary, background = surface, minimum = TEXT)
            assertContrast(foreground = colors.textSecondary, background = surface, minimum = TEXT)
            assertContrast(foreground = colors.primary, background = surface, minimum = TEXT)
            assertContrast(foreground = colors.error, background = surface, minimum = TEXT)
        }
        // Never on surfaceHighest
        listOf(colors.background, colors.surface, colors.surfaceHigh).forEach { surface ->
            assertContrast(foreground = colors.textTertiary, background = surface, minimum = TEXT)
        }
    }

    @Test
    fun `the texts on the accents are readable`() {
        assertContrast(foreground = colors.onPrimary, background = colors.primary, minimum = TEXT)
        assertContrast(foreground = colors.onError, background = colors.error, minimum = TEXT)
    }

    @Test
    fun `the boundary of a field is visible on the surfaces it is drawn on`() {
        // A field and its surroundings: the background, a card, the field itself
        listOf(colors.background, colors.surface, colors.surfaceHigh).forEach { surface ->
            assertContrast(foreground = colors.outline, background = surface, minimum = CONTROL)
            assertContrast(foreground = colors.primary, background = surface, minimum = CONTROL)
            assertContrast(foreground = colors.error, background = surface, minimum = CONTROL)
        }
    }

    private fun assertContrast(foreground: Color, background: Color, minimum: Float) {
        val lighter = max(foreground.luminance(), background.luminance())
        val darker = min(foreground.luminance(), background.luminance())
        val ratio = (lighter + 0.05f) / (darker + 0.05f)
        assertTrue(
            actual = ratio >= minimum,
            message = "Contrast of $foreground on $background: $ratio, below $minimum",
        )
    }

    private companion object {
        const val TEXT = 4.5f
        const val CONTROL = 3f
    }
}
