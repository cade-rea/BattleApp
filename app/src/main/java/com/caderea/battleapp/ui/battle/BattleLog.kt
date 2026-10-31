package com.caderea.battleapp.ui.battle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.LogEntry
import com.caderea.battleapp.viewmodel.Side

/**
 * The battle log, newest at the bottom. Each line starts with its tick and a dot in the
 * color of whoever it's about.
 */
@Composable
fun BattleLog(log: List<LogEntry>, playerColor: RgbColor, enemyColor: RgbColor, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BattleColors.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        // laid out from the bottom up, so the newest line stays in view
        reverseLayout = true,
    ) {
        items(log.asReversed()) { entry ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.tick.toString(),
                    Modifier.width(24.dp),
                    color = BattleColors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                )
                Spacer(Modifier.width(8.dp))
                val dot = when (entry.side) {
                    Side.PLAYER -> playerColor.readable
                    Side.ENEMY -> enemyColor.readable
                    null -> null
                }
                Canvas(Modifier.size(6.dp)) {
                    if (dot != null) drawCircle(dot)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    entry.text,
                    color = if (entry.side == null) BattleColors.textMuted else BattleColors.text,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}
