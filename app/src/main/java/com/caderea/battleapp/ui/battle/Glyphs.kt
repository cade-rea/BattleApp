package com.caderea.battleapp.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.caderea.battleapp.viewmodel.ActionKind

/**
 * A small abstract mark for what kind of action something is: a spike for attacks, a plus
 * for heals and a shield for guards.
 */
@Composable
fun KindGlyph(kind: ActionKind, color: Color, modifier: Modifier = Modifier, size: Dp = 16.dp) {
    Canvas(modifier.size(size)) {
        drawKindGlyph(kind, color, Offset.Zero, this.size)
    }
}

/** Draws [kind]'s mark in the box at [topLeft] of [size]. */
fun DrawScope.drawKindGlyph(kind: ActionKind, color: Color, topLeft: Offset, size: Size) {
    val w = size.width
    val h = size.height
    val x = topLeft.x
    val y = topLeft.y

    when (kind) {
        ActionKind.ATTACK -> {
            // a spike pointing up and right, towards the opponent
            val path = Path().apply {
                moveTo(x + w, y)
                lineTo(x + w * 0.62f, y + h)
                lineTo(x + w * 0.45f, y + h * 0.55f)
                lineTo(x, y + h * 0.38f)
                close()
            }
            drawPath(path, color)
        }

        ActionKind.HEAL -> {
            val stroke = w * 0.28f
            drawLine(color, Offset(x + w / 2, y + h * 0.1f), Offset(x + w / 2, y + h * 0.9f), stroke, StrokeCap.Round)
            drawLine(color, Offset(x + w * 0.1f, y + h / 2), Offset(x + w * 0.9f, y + h / 2), stroke, StrokeCap.Round)
        }

        ActionKind.GUARD -> {
            val path = Path().apply {
                moveTo(x + w / 2, y)
                lineTo(x + w * 0.95f, y + h * 0.18f)
                cubicTo(x + w * 0.95f, y + h * 0.65f, x + w * 0.7f, y + h * 0.88f, x + w / 2, y + h)
                cubicTo(x + w * 0.3f, y + h * 0.88f, x + w * 0.05f, y + h * 0.65f, x + w * 0.05f, y + h * 0.18f)
                close()
            }
            drawPath(path, color)
        }
    }
}
