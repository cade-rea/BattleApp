package com.caderea.battleapp.ui.arena

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.caderea.battleapp.R
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.environment.Light
import com.caderea.battleapp.ui.battle.color
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import kotlin.math.roundToInt

/** The tallest multiplier the bars leave room for. */
private const val BAR_SCALE = 1.3f

/**
 * A light as three bars, red, green and blue, each as tall as the light on that channel.
 * A tick marks plain light, so a boost pokes above it and a cut falls short.
 */
@Composable
fun LightBars(light: Light, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val gap = size.width * 0.12f
        val barWidth = (size.width - gap * 2) / 3
        val plainY = size.height * (1 - 1 / BAR_SCALE)
        Channel.entries.forEachIndexed { index, channel ->
            val value = light[channel].toFloat()
            val left = index * (barWidth + gap)
            val top = size.height * (1 - value / BAR_SCALE)
            val color = channel.color.compose
            drawRoundRect(BattleColors.surfaceRaised, Offset(left, 0f), Size(barWidth, size.height), CornerRadius(2f))
            drawRoundRect(
                color.copy(alpha = if (value >= 1f) 1f else 0.55f),
                Offset(left, top),
                Size(barWidth, size.height - top),
                CornerRadius(2f),
            )
            drawLine(BattleColors.text.copy(alpha = 0.6f), Offset(left, plainY), Offset(left + barWidth, plainY), 1.5f)
        }
    }
}

/** A faint wash of the light's favored color from the top, or nothing in neutral light. */
fun lightWash(light: Light): Brush? {
    val channel = light.favored ?: return null
    return Brush.verticalGradient(
        0f to channel.color.compose.copy(alpha = 0.10f),
        0.35f to Color.Transparent,
    )
}

/** The text color for a light: its favored channel, or plain text. */
fun lightTextColor(light: Light): Color = light.favored?.color?.readable ?: BattleColors.text

/** A short label for a light, such as "Red +10%", "All colors 70%" or "Plain light". */
@Composable
fun lightLabel(light: Light): String {
    if (light.isPlain) return stringResource(R.string.light_plain)

    val values = Channel.entries.map { light[it] }
    if (values.distinct().size == 1) return stringResource(R.string.light_all, percent(values.first()))

    return Channel.entries
        .filter { light[it] != 1.0 }
        .map { channel ->
            val name = channelName(channel)
            val value = light[channel]
            if (value > 1) {
                stringResource(R.string.light_boost, name, percent(value - 1))
            } else {
                stringResource(R.string.light_cut, name, percent(value))
            }
        }
        .joinToString(" · ")
}

@Composable
fun channelName(channel: Channel): String = stringResource(
    when (channel) {
        Channel.RED -> R.string.channel_red
        Channel.GREEN -> R.string.channel_green
        Channel.BLUE -> R.string.channel_blue
    }
)

private fun percent(share: Double) = (share * 100).roundToInt()
