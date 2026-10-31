package com.caderea.battleapp.ui.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.ui.battle.TypeCycleLegend
import com.caderea.battleapp.ui.fighter.FighterSummaryCard
import com.caderea.battleapp.ui.battle.color
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.FighterSummary

/**
 * The title, the player's fighter, and the ways in: a battle, the loadout, or the map.
 */
@Composable
fun MainMenuScreen(
    fighter: FighterSummary,
    onBattle: () -> Unit,
    onLoadout: () -> Unit,
    onMap: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BattleColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PrimaryOrbs()
        Spacer(Modifier.height(28.dp))
        Title()
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.menu_tagline), color = BattleColors.textMuted, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        TypeCycleLegend()
        Spacer(Modifier.height(36.dp))
        FighterSummaryCard(fighter, Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))

        Button(onBattle, Modifier.fillMaxWidth().height(52.dp)) {
            Text(stringResource(R.string.button_battle), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onLoadout, Modifier.fillMaxWidth().height(52.dp).testTag("loadout")) {
            Text(stringResource(R.string.button_loadout))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onMap, Modifier.fillMaxWidth().height(52.dp)) {
            Text(stringResource(R.string.button_map), color = BattleColors.textMuted)
        }
    }
}

/** "BATTLE" with its letters cycling through the primaries. */
@Composable
private fun Title() {
    val title = stringResource(R.string.app_name).uppercase()
    val text = buildAnnotatedString {
        title.forEachIndexed { index, letter ->
            val channel = Channel.entries[index % Channel.entries.size]
            withStyle(SpanStyle(color = channel.color.readable)) { append(letter) }
        }
    }
    Text(text, fontSize = 44.sp, fontWeight = FontWeight.Black, letterSpacing = 8.sp)
}

/** The three primaries as overlapping glowing orbs. */
@Composable
private fun PrimaryOrbs() {
    Row(horizontalArrangement = Arrangement.spacedBy((-18).dp)) {
        Channel.entries.forEach { channel ->
            val color = channel.color.compose
            Canvas(Modifier.size(64.dp)) {
                drawCircle(
                    Brush.radialGradient(
                        0f to Color.White.copy(alpha = 0.85f),
                        0.3f to color,
                        1f to color.copy(alpha = 0f),
                    )
                )
            }
        }
    }
}
