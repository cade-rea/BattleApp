package com.caderea.battleapp.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.core.BattleQueue
import com.caderea.battleapp.core.QueuedAction
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.ColorTraits
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.FighterState
import com.caderea.battleapp.viewmodel.kind

private val GUTTER = 18.dp
private val LANE_HEIGHT = 44.dp
private val AXIS_HEIGHT = 22.dp

/** Ticks the timeline shows: long enough for a full queue of the slowest color. */
private val SPAN_TICKS = BattleQueue.MAX_SIZE * ColorTraits.of(RgbColor.BLUE).windUpTicks

/**
 * Both fighters' queues on one shared clock: the enemy's lane on top, the player's below,
 * and "now" on the left. Each action is a block in its own color that ends on the tick it
 * lands, so the blocks slide left as time passes, and lining up a guard against an incoming
 * attack is a matter of seeing whether the two overlap.
 *
 * @param tickFraction how far through the current tick we are, 0 to 1
 * @param onLaneAnchors where each lane meets "now", in root coordinates, for effects drawn
 * over the whole screen
 */
@Composable
fun Timeline(
    enemy: FighterState,
    enemyQueue: List<QueuedAction>,
    player: FighterState,
    playerQueue: List<QueuedAction>,
    tickFraction: Float,
    modifier: Modifier = Modifier,
    onLaneAnchors: (enemy: Offset, player: Offset) -> Unit = { _, _ -> },
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BattleColors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.timeline_title),
                color = BattleColors.textMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(8.dp))

        Canvas(
            Modifier
                .fillMaxWidth()
                .height(LANE_HEIGHT * 2 + AXIS_HEIGHT)
                .onGloballyPositioned { coordinates ->
                    with(density) {
                        val origin = coordinates.positionInRoot() + Offset(GUTTER.toPx(), LANE_HEIGHT.toPx() / 2)
                        onLaneAnchors(origin, origin + Offset(0f, (LANE_HEIGHT + AXIS_HEIGHT).toPx()))
                    }
                }
        ) {
            val gutter = GUTTER.toPx()
            val laneHeight = LANE_HEIGHT.toPx()
            val axisHeight = AXIS_HEIGHT.toPx()
            val tickWidth = (size.width - gutter) / SPAN_TICKS
            val enemyLane = 0f
            val playerLane = laneHeight + axisHeight

            // the tick grid
            for (tick in 0..SPAN_TICKS) {
                val x = gutter + tick * tickWidth
                drawLine(BattleColors.outline, Offset(x, 0f), Offset(x, size.height), 1.dp.toPx())
                if (tick > 0) {
                    val label = textMeasurer.measure(
                        tick.toString(),
                        TextStyle(color = BattleColors.textMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                    )
                    drawText(
                        label,
                        topLeft = Offset(
                            x - label.size.width / 2f,
                            laneHeight + (axisHeight - label.size.height) / 2f,
                        ),
                    )
                }
            }

            drawLane(enemy, enemyQueue, enemyLane, laneHeight, gutter, tickWidth, tickFraction, textMeasurer)
            drawLane(player, playerQueue, playerLane, laneHeight, gutter, tickWidth, tickFraction, textMeasurer)

            // "now"
            drawLine(BattleColors.text, Offset(gutter, 0f), Offset(gutter, size.height), 2.dp.toPx())
        }
    }
}

/**
 * One fighter's lane: a dot in their aura color, any guard that's up as a band, and their
 * queued actions as blocks.
 */
private fun DrawScope.drawLane(
    fighter: FighterState,
    queue: List<QueuedAction>,
    top: Float,
    height: Float,
    gutter: Float,
    tickWidth: Float,
    tickFraction: Float,
    textMeasurer: TextMeasurer,
) {
    drawCircle(fighter.color.readable, radius = 5.dp.toPx(), center = Offset(gutter / 2 - 2.dp.toPx(), top + height / 2))

    clipRect(left = gutter, top = top, bottom = top + height) {
        fighter.guard?.let { guard ->
            // up until the end of its last tick
            val right = gutter + (guard.ticksLeft - tickFraction) * tickWidth
            val color = guard.color.readable
            drawRect(color.copy(alpha = 0.16f), Offset(gutter, top), Size(right - gutter, height))
            drawRect(color, Offset(gutter, top), Size(right - gutter, 3.dp.toPx()))
        }

        var start = 0f
        for ((index, queued) in queue.withIndex()) {
            // only the front action winds up, so only it moves through the current tick
            val remaining = queued.ticksLeft - if (index == 0) tickFraction else 0f
            val end = start + remaining
            drawBlock(
                queued,
                isFront = index == 0,
                left = gutter + start * tickWidth,
                right = gutter + end * tickWidth,
                top = top,
                height = height,
                textMeasurer = textMeasurer,
            )
            start = end
        }
    }
}

private fun DrawScope.drawBlock(
    queued: QueuedAction,
    isFront: Boolean,
    left: Float,
    right: Float,
    top: Float,
    height: Float,
    textMeasurer: TextMeasurer,
) {
    val inset = 2.dp.toPx()
    val blockTop = top + 5.dp.toPx()
    val blockHeight = height - 10.dp.toPx()
    val width = right - left - inset * 2
    if (width <= 0f) return

    val action = queued.action
    val color = action.color.compose
    val corner = CornerRadius(6.dp.toPx())
    val topLeft = Offset(left + inset, blockTop)
    val blockSize = Size(width, blockHeight)

    // the action winding up is solid; the ones waiting behind it are outlines
    val textColor = if (isFront) {
        drawRoundRect(color, topLeft, blockSize, corner)
        if (color.luminance() > 0.35f) Color.Black else Color.White
    } else {
        drawRoundRect(color.copy(alpha = 0.22f), topLeft, blockSize, corner)
        drawRoundRect(action.color.readable, topLeft, blockSize, corner, style = Stroke(1.5.dp.toPx()))
        action.color.readable
    }

    val glyphSize = 11.dp.toPx()
    val padding = 5.dp.toPx()
    if (width < glyphSize + padding * 2) return
    drawKindGlyph(
        action.kind,
        textColor,
        Offset(topLeft.x + padding, blockTop + (blockHeight - glyphSize) / 2),
        Size(glyphSize, glyphSize),
    )

    val textLeft = topLeft.x + padding * 2 + glyphSize
    val textWidth = topLeft.x + width - padding - textLeft
    if (textWidth < 16.dp.toPx()) return
    val label = textMeasurer.measure(
        action.name,
        TextStyle(color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        constraints = Constraints(maxWidth = textWidth.toInt()),
    )
    drawText(label, topLeft = Offset(textLeft, blockTop + (blockHeight - label.size.height) / 2))
}

/** A reminder of the type cycle: red beats green beats blue beats red. */
@Composable
fun TypeCycleLegend(modifier: Modifier = Modifier) {
    val text = buildAnnotatedString {
        val cycle = Channel.entries + Channel.entries.first()
        cycle.forEachIndexed { index, channel ->
            if (index > 0) {
                withStyle(SpanStyle(color = BattleColors.textMuted)) { append(" › ") }
            }
            withStyle(SpanStyle(color = channel.color.readable, fontWeight = FontWeight.Bold)) {
                append(channel.name.first())
            }
        }
    }
    Text(text, modifier, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
}

/** The pure color of a channel. */
val Channel.color: RgbColor
    get() = when (this) {
        Channel.RED -> RgbColor.RED
        Channel.GREEN -> RgbColor.GREEN
        Channel.BLUE -> RgbColor.BLUE
    }
