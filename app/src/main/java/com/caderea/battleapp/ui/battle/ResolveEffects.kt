package com.caderea.battleapp.ui.battle

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.ActionKind
import com.caderea.battleapp.viewmodel.BattleUiState
import com.caderea.battleapp.viewmodel.Side
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Where the effects start and end, in root coordinates: each lane's "now" point on the
 * timeline, and each fighter's aura orb. Unspecified until the screen is laid out.
 */
class EffectAnchors {
    var enemyLane by mutableStateOf(Offset.Unspecified)
    var playerLane by mutableStateOf(Offset.Unspecified)
    var enemyOrb by mutableStateOf(Offset.Unspecified)
    var playerOrb by mutableStateOf(Offset.Unspecified)

    fun lane(side: Side) = if (side == Side.PLAYER) playerLane else enemyLane

    fun orb(side: Side) = if (side == Side.PLAYER) playerOrb else enemyOrb
}

/**
 * Plays out what landed on the last tick, drawn over the whole screen: the lane pulses
 * where the action left the queue, a streak in the action's color flies to whoever it hits
 * (the opponent for attacks, the performer for heals and guards), a ring bursts there, and
 * the health change floats up off the orb.
 *
 * @param durationMs how long it plays; keep it under a tick so effects don't pile up
 */
@Composable
fun ResolveEffects(state: BattleUiState, anchors: EffectAnchors, durationMs: Int, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(1f) }
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(state.tick) {
        if (state.landed.isEmpty()) return@LaunchedEffect
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMs, easing = LinearEasing))
    }

    Canvas(modifier) {
        val p = progress.value
        if (p >= 1f) return@Canvas

        for (landed in state.landed) {
            val from = anchors.lane(landed.side)
            val target = if (landed.kind == ActionKind.ATTACK) landed.side.opponent else landed.side
            val to = anchors.orb(target)
            if (from == Offset.Unspecified || to == Offset.Unspecified) continue

            val color = landed.color.readable
            drawLanePulse(from, color, phase(p, 0f, 0.35f))
            drawStreak(from, to, color, phase(p, 0f, 0.45f), phase(p, 0.12f, 0.55f))
            drawImpact(to, color, phase(p, 0.4f, 1f))
        }

        // the health change floats up off the orb
        val rise = phase(p, 0.4f, 1f)
        if (rise > 0f) {
            for (side in Side.entries) {
                val change = if (side == Side.PLAYER) state.player.healthChange else state.enemy.healthChange
                val orb = anchors.orb(side)
                if (change == 0 || orb == Offset.Unspecified) continue

                val text = textMeasurer.measure(
                    if (change > 0) "+$change" else change.toString(),
                    TextStyle(
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        shadow = Shadow(Color.Black, blurRadius = 8f),
                    ),
                )
                // centered on the orb, so it doesn't cover the health numbers
                drawText(
                    text,
                    topLeft = orb - Offset(text.size.width / 2f, text.size.height / 2f + rise * 30.dp.toPx()),
                    alpha = 1 - rise * rise,
                )
            }
        }
    }
}

private val Side.opponent: Side
    get() = if (this == Side.PLAYER) Side.ENEMY else Side.PLAYER

/** How far [p] is through the window from [start] to [end], from 0 to 1. */
private fun phase(p: Float, start: Float, end: Float) = ((p - start) / (end - start)).coerceIn(0f, 1f)

/** A ring that swells and fades where the action leaves the queue. */
private fun DrawScope.drawLanePulse(at: Offset, color: Color, t: Float) {
    if (t <= 0f || t >= 1f) return
    drawCircle(
        color.copy(alpha = 1 - t),
        radius = lerp(6.dp.toPx(), 30.dp.toPx(), t),
        center = at,
        style = Stroke(3.dp.toPx()),
    )
}

/**
 * A streak from [from] to [to]: its head is [head] of the way there and its tail [tail], so
 * it stretches out and then catches up. The head carries an arrowhead until it arrives.
 */
private fun DrawScope.drawStreak(from: Offset, to: Offset, color: Color, head: Float, tail: Float) {
    if (tail >= 1f) return
    val headAt = lerp(from, to, FastOutSlowInEasing.transform(head))
    val tailAt = lerp(from, to, FastOutSlowInEasing.transform(tail))

    drawLine(color.copy(alpha = 0.25f), tailAt, headAt, 16.dp.toPx(), StrokeCap.Round)
    drawLine(color, tailAt, headAt, 5.dp.toPx(), StrokeCap.Round)

    if (head < 1f) {
        val angle = atan2(to.y - from.y, to.x - from.x)
        val length = 16.dp.toPx()
        val spread = 0.5f
        val arrow = Path().apply {
            moveTo(headAt.x + cos(angle) * length * 0.6f, headAt.y + sin(angle) * length * 0.6f)
            lineTo(headAt.x - cos(angle - spread) * length, headAt.y - sin(angle - spread) * length)
            lineTo(headAt.x - cos(angle + spread) * length, headAt.y - sin(angle + spread) * length)
            close()
        }
        drawPath(arrow, color)
    }
}

/** A flash and a ring bursting out from whoever the action reached. */
private fun DrawScope.drawImpact(at: Offset, color: Color, t: Float) {
    if (t <= 0f || t >= 1f) return
    drawCircle(color.copy(alpha = 0.35f * (1 - t)), radius = 34.dp.toPx(), center = at)
    drawCircle(
        color.copy(alpha = 1 - t),
        radius = lerp(26.dp.toPx(), 64.dp.toPx(), t),
        center = at,
        style = Stroke(lerp(6.dp.toPx(), 1.dp.toPx(), t)),
    )
}
