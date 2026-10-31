package com.caderea.battleapp.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.ui.fighter.FighterSummaryCard
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.CharacterRow
import com.caderea.battleapp.viewmodel.CharactersUiState

/**
 * Every saved character. Tapping one loads it; the loaded one is outlined in its aura.
 * Deleting asks first, since it can't be undone.
 */
@Composable
fun CharactersScreen(
    state: CharactersUiState,
    onSelect: (id: String) -> Unit,
    onDelete: (id: String) -> Unit,
    onNew: () -> Unit,
    onBack: () -> Unit,
) {
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }

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
                stringResource(R.string.characters_title),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onBack) { Text(stringResource(R.string.button_back), color = BattleColors.textMuted) }
        }
        Text(stringResource(R.string.characters_hint), color = BattleColors.textMuted, fontSize = 13.sp)
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            items(state.characters, key = { it.id }) { row ->
                CharacterItem(row, onSelect = { onSelect(row.id) }, onDelete = { deleting = row.id })
            }
        }
        Button(onNew, Modifier.fillMaxWidth().height(52.dp).testTag("newCharacter")) {
            Text(stringResource(R.string.button_new_character), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    state.characters.firstOrNull { it.id == deleting }?.let { row ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text(stringResource(R.string.delete_title, row.fighter.name)) },
            text = { Text(stringResource(R.string.delete_message)) },
            confirmButton = {
                TextButton({ onDelete(row.id); deleting = null }, Modifier.testTag("confirmDelete")) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton({ deleting = null }) { Text(stringResource(R.string.button_keep), color = BattleColors.textMuted) }
            },
            containerColor = BattleColors.surfaceRaised,
        )
    }
}

@Composable
private fun CharacterItem(row: CharacterRow, onSelect: () -> Unit, onDelete: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BattleColors.surface)
            .border(
                if (row.active) 2.dp else 1.dp,
                if (row.active) row.fighter.color.readable else BattleColors.outline,
                shape,
            )
            .clickable(onClick = onSelect)
            .testTag("character${row.fighter.name}")
    ) {
        FighterSummaryCard(row.fighter, Modifier.fillMaxWidth())
        Row(
            Modifier.fillMaxWidth().background(BattleColors.surface).padding(start = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(if (row.active) R.string.characters_loaded else R.string.characters_tap_to_load),
                Modifier.weight(1f),
                color = if (row.active) BattleColors.text else BattleColors.textMuted,
                fontSize = 12.sp,
            )
            TextButton(onDelete, Modifier.testTag("delete${row.fighter.name}")) {
                Text(stringResource(R.string.button_delete), color = BattleColors.textMuted, fontSize = 12.sp)
            }
        }
    }
}
