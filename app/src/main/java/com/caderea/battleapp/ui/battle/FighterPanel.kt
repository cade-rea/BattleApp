package com.caderea.battleapp.ui.battle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.core.fighter.Fighter
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.ActionKind
import com.caderea.battleapp.viewmodel.FighterState

/**
 * One fighter at a glance: their aura, health, energy and any guard that's up.
 */
@Composable
fun FighterPanel(fighter: FighterState, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BattleColors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraOrb(fighter, Modifier.size(52.dp))
        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    fighter.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    fighter.color.hex,
                    color = fighter.color.readable,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                )
                Spacer(Modifier.weight(1f))
                fighter.guard?.let { guard ->
                    KindGlyph(ActionKind.GUARD, guard.color.readable, size = 12.dp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        stringResource(R.string.guard_up, guard.name, guard.ticksLeft),
                        color = guard.color.readable,
                        fontSize = 12.sp,
                    )
                }
            }
            HealthBar(fighter)
            EnergyBar(fighter)
        }
    }
}

/**
 * The fighter's aura as a glowing orb in its own color. A guard that's up circles it in the
 * guard's color.
 */
@Composable
private fun AuraOrb(fighter: FighterState, modifier: Modifier = Modifier) {
    val aura = fighter.color.compose
    val guard = fighter.guard?.color?.readable

    Canvas(modifier) {
        val radius = size.minDimension / 2
        drawCircle(
            Brush.radialGradient(
                0f to Color.White.copy(alpha = 0.9f),
                0.25f to aura,
                0.7f to aura.copy(alpha = 0.55f),
                1f to aura.copy(alpha = 0f),
                center = center,
                radius = radius,
            ),
            radius,
        )
        if (guard != null) {
            drawCircle(guard, radius - 2.dp.toPx(), style = Stroke(width = 3.dp.toPx()))
        }
    }
}

/**
 * Health in the fighter's color. Damage leaves a pale trail that drains a moment later, so
 * a hit is easy to see even mid-glance.
 */
@Composable
private fun HealthBar(fighter: FighterState) {
    val fraction = if (fighter.maxHealth == 0) 0f else fighter.health.toFloat() / fighter.maxHealth
    val shown by animateFloatAsState(fraction, tween(250), label = "health")
    val trail by animateFloatAsState(fraction, tween(700, delayMillis = 350), label = "health trail")
    val color = fighter.color.readable

    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(
            Modifier
                .weight(1f)
                .height(12.dp)
        ) {
            val corner = CornerRadius(size.height / 2)
            drawRoundRect(BattleColors.surfaceRaised, cornerRadius = corner)
            drawRoundRect(Color.White.copy(alpha = 0.55f), size = Size(size.width * trail, size.height), cornerRadius = corner)
            drawRoundRect(color, size = Size(size.width * shown, size.height), cornerRadius = corner)
        }
        Text(
            stringResource(R.string.health_value, fighter.health, fighter.maxHealth),
            Modifier.width(92.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * Energy as segments of [Fighter.ENERGY_PER_TICK], so each segment is one tick of regen.
 */
@Composable
private fun EnergyBar(fighter: FighterState) {
    val shown by animateFloatAsState(fighter.energy.toFloat(), tween(200), label = "energy")
    val segments = (fighter.maxEnergy / Fighter.ENERGY_PER_TICK).coerceAtLeast(1)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(
            Modifier
                .weight(1f)
                .height(8.dp)
        ) {
            val gap = 3.dp.toPx()
            val segmentWidth = (size.width - gap * (segments - 1)) / segments
            val corner = CornerRadius(2.dp.toPx())
            for (i in 0 until segments) {
                val left = i * (segmentWidth + gap)
                drawRoundRect(BattleColors.surfaceRaised, Offset(left, 0f), Size(segmentWidth, size.height), corner)

                val filled = ((shown - i * Fighter.ENERGY_PER_TICK) / Fighter.ENERGY_PER_TICK).coerceIn(0f, 1f)
                if (filled > 0f) {
                    drawRoundRect(BattleColors.energy, Offset(left, 0f), Size(segmentWidth * filled, size.height), corner)
                }
            }
        }
        Text(
            stringResource(R.string.energy_value, fighter.energy, fighter.maxEnergy),
            Modifier.width(92.dp),
            color = BattleColors.textMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            textAlign = TextAlign.End,
        )
    }
}
