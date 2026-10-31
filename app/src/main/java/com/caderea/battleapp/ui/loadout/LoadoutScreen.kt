package com.caderea.battleapp.ui.loadout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.ui.arena.ArenaCard
import com.caderea.battleapp.ui.fighter.FighterSummaryCard
import com.caderea.battleapp.ui.technique.TechniqueCard
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.viewmodel.LoadoutUiState

/**
 * The fighter's catalog, with the techniques they'll bring marked by their button number.
 * Tapping a technique adds it to the loadout or takes it out. Before a fight it shows the
 * arena, and the techniques as they'd work in its light; otherwise its button goes on to
 * choose an arena.
 */
@Composable
fun LoadoutScreen(state: LoadoutUiState, onToggle: (id: Int) -> Unit, onBattle: () -> Unit, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BattleColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.loadout_title),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onBack) { Text(stringResource(R.string.button_back), color = BattleColors.textMuted) }
        }
        FighterSummaryCard(state.fighter, Modifier.fillMaxWidth())
        state.arena?.let { ArenaCard(it, Modifier.fillMaxWidth().testTag("arena")) }
        Text(
            if (state.arena != null) {
                stringResource(R.string.loadout_hint, state.chosen, state.slots)
            } else {
                stringResource(R.string.loadout_hint_any, state.slots)
            },
            color = BattleColors.textMuted,
            fontSize = 13.sp,
        )
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            items(state.techniques, key = { it.id }) { technique ->
                TechniqueCard(
                    technique,
                    Modifier
                        .fillMaxWidth()
                        .testTag("technique${technique.id}"),
                    onClick = { onToggle(technique.id) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Button(onBattle, Modifier.fillMaxWidth().height(52.dp).testTag("battle")) {
            Text(
                stringResource(if (state.arena != null) R.string.button_battle else R.string.button_choose_arena),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
