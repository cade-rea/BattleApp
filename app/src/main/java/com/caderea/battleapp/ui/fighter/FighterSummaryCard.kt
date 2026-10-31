package com.caderea.battleapp.ui.fighter

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.FighterSummary

/**
 * The fighter between battles: their aura, level, progress to the next level and record.
 */
@Composable
fun FighterSummaryCard(fighter: FighterSummary, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BattleColors.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuraGlow(fighter.color, size = 56.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(fighter.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(8.dp))
                Text(
                    fighter.color.hex,
                    color = fighter.color.readable,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.fighter_record, fighter.wins, fighter.losses),
                    color = BattleColors.textMuted,
                    fontSize = 12.sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.fighter_level, fighter.level),
                    Modifier.width(64.dp),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                )
                LevelBar(fighter.levelProgress, fighter.color, Modifier.weight(1f))
            }
        }
    }
}

/** Progress to the next level, in the aura's color. */
@Composable
fun LevelBar(progress: Float, color: RgbColor, modifier: Modifier = Modifier) {
    val fill = color.readable
    Canvas(modifier.height(6.dp)) {
        val corner = CornerRadius(size.height / 2)
        drawRoundRect(BattleColors.surfaceRaised, cornerRadius = corner)
        drawRoundRect(fill, size = Size(size.width * progress.coerceIn(0f, 1f), size.height), cornerRadius = corner)
    }
}

/** An aura as a soft glowing orb. */
@Composable
fun AuraGlow(color: RgbColor, size: Dp, modifier: Modifier = Modifier) {
    val aura = color.compose
    Canvas(modifier.size(size)) {
        drawCircle(
            Brush.radialGradient(
                0f to Color.White.copy(alpha = 0.85f),
                0.25f to aura,
                0.7f to aura.copy(alpha = 0.5f),
                1f to aura.copy(alpha = 0f),
            )
        )
    }
}
