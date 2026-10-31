package com.caderea.battleapp.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * The type cycle as a triangle: red at the top, then arrows round to green and blue, each
 * arrow pointing from a color to the one it beats. The fighters' aura colors are ringed and
 * labeled, so the matchup reads at a glance.
 */
@Composable
fun ColorTriangle(player: RgbColor, enemy: RgbColor, youLabel: String, foeLabel: String, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    val playerChannel = player.dominantChannel
    val enemyChannel = enemy.dominantChannel

    val description = stringResource(R.string.type_cycle_description)

    Canvas(modifier.semantics { contentDescription = description }) {
        val nodeRadius = 15.dp.toPx()
        val labelRoom = 16.dp.toPx()
        // the triangle's circumradius, leaving room for the labels outside it
        val radius = minOf(size.width / 2 - nodeRadius - labelRoom * 2, (size.height - nodeRadius * 2 - labelRoom * 2) / 1.5f)
        val center = Offset(size.width / 2, nodeRadius + labelRoom + radius)

        // red at the top, then clockwise: green bottom right, blue bottom left
        val nodes = Channel.entries.associateWith { channel ->
            val angle = Math.toRadians(-90.0 + 120.0 * channel.ordinal)
            center + Offset((radius * cos(angle)).toFloat(), (radius * sin(angle)).toFloat())
        }

        for (channel in Channel.entries) {
            drawArrow(nodes.getValue(channel), nodes.getValue(channel.prey), nodeRadius + 4.dp.toPx(), BattleColors.textMuted)
        }

        for ((channel, at) in nodes) {
            val color = channel.color.compose
            drawCircle(
                Brush.radialGradient(
                    0f to Color.White.copy(alpha = 0.85f),
                    0.35f to color,
                    1f to color.copy(alpha = 0.15f),
                    center = at,
                    radius = nodeRadius,
                ),
                nodeRadius,
                at,
            )

            val labels = listOfNotNull(
                youLabel.takeIf { channel == playerChannel },
                foeLabel.takeIf { channel == enemyChannel },
            )
            if (labels.isNotEmpty()) {
                drawCircle(Color.White, nodeRadius + 4.dp.toPx(), at, style = Stroke(2.dp.toPx()))
                drawLabel(textMeasurer, labels.joinToString(" · "), at, nodeRadius + 6.dp.toPx(), channel.color.readable, below = channel != Channel.RED)
            }
        }
    }
}

/** The channel with the most power in it, the color's main type. */
private val RgbColor.dominantChannel: Channel
    get() = Channel.entries.maxBy { this[it] }

private fun DrawScope.drawArrow(from: Offset, to: Offset, inset: Float, color: Color) {
    val length = hypot(to.x - from.x, to.y - from.y)
    val direction = Offset((to.x - from.x) / length, (to.y - from.y) / length)
    val start = from + direction * inset
    val end = to - direction * inset
    val stroke = 2.dp.toPx()
    drawLine(color, start, end - direction * 6.dp.toPx(), stroke)

    val head = 9.dp.toPx()
    val normal = Offset(-direction.y, direction.x)
    val arrow = Path().apply {
        moveTo(end.x, end.y)
        val back = end - direction * head
        lineTo(back.x + normal.x * head / 2, back.y + normal.y * head / 2)
        lineTo(back.x - normal.x * head / 2, back.y - normal.y * head / 2)
        close()
    }
    drawPath(arrow, color)
}

private fun DrawScope.drawLabel(
    textMeasurer: TextMeasurer,
    text: String,
    at: Offset,
    offset: Float,
    color: Color,
    below: Boolean,
) {
    val label = textMeasurer.measure(text, TextStyle(color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold))
    val y = if (below) at.y + offset else at.y - offset - label.size.height
    drawText(label, topLeft = Offset(at.x - label.size.width / 2f, y))
}
