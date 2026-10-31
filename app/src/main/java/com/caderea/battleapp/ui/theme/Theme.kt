package com.caderea.battleapp.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.caderea.battleapp.core.color.RgbColor

/**
 * The app's look: near-black and greys, so the only saturated colors on screen are the
 * fighters' and techniques' own colors. Color always means type and power, never UI chrome.
 */
object BattleColors {
    val background = Color(0xFF0E0F13)
    val surface = Color(0xFF171920)
    val surfaceRaised = Color(0xFF20232B)
    val outline = Color(0xFF353944)
    val text = Color(0xFFE9EAEE)
    val textMuted = Color(0xFF8B909B)
    // energy isn't a type, so it's drawn in white
    val energy = Color(0xFFF2F2F2)
}

private val colorScheme = darkColorScheme(
    primary = BattleColors.text,
    onPrimary = BattleColors.background,
    secondary = BattleColors.textMuted,
    background = BattleColors.background,
    onBackground = BattleColors.text,
    surface = BattleColors.surface,
    onSurface = BattleColors.text,
    surfaceVariant = BattleColors.surfaceRaised,
    onSurfaceVariant = BattleColors.textMuted,
    outline = BattleColors.outline,
)

@Composable
fun BattleTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colorScheme) {
        // text drawn straight on the background, outside any Surface, is light too
        CompositionLocalProvider(LocalContentColor provides BattleColors.text, content = content)
    }
}

/** The color as Compose draws it. */
val RgbColor.compose: Color
    get() = Color(argb)

/**
 * A version of the color bright enough to read as text or a thin line on the dark
 * background. Pure blue is too dark on its own, so dark colors are lifted towards white.
 */
val RgbColor.readable: Color
    get() {
        val color = compose
        val lift = (0.45f - luminance(color)).coerceIn(0f, 0.45f)
        return Color(
            red = color.red + (1 - color.red) * lift,
            green = color.green + (1 - color.green) * lift,
            blue = color.blue + (1 - color.blue) * lift,
        )
    }

private fun luminance(color: Color) = 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
