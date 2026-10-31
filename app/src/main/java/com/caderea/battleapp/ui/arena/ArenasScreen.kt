package com.caderea.battleapp.ui.arena

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.ui.fighter.FighterSummaryCard
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.viewmodel.ArenaCardState
import com.caderea.battleapp.viewmodel.FighterSummary

/**
 * Where to fight. Each arena shows its light as bars, how many techniques it takes and any
 * level cap. Tapping one goes on to pick the loadout for it.
 */
@Composable
fun ArenasScreen(
    fighter: FighterSummary?,
    arenas: List<ArenaCardState>,
    onPick: (id: String) -> Unit,
    onBack: () -> Unit,
) {
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
                stringResource(R.string.arenas_title),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onBack) { Text(stringResource(R.string.button_back), color = BattleColors.textMuted) }
        }
        fighter?.let { FighterSummaryCard(it, Modifier.fillMaxWidth()) }
        Text(stringResource(R.string.arenas_hint), color = BattleColors.textMuted, fontSize = 13.sp)
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
        ) {
            items(arenas, key = { it.id }) { arena ->
                ArenaCard(
                    arena,
                    Modifier
                        .fillMaxWidth()
                        .testTag("arena_${arena.id}"),
                    onClick = { onPick(arena.id) },
                )
            }
        }
    }
}

/** One arena: its light, name, rules in a line, and what it takes. */
@Composable
fun ArenaCard(arena: ArenaCardState, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(14.dp)
    val first = arena.lights.first()
    val wash = lightWash(first)
    Row(
        modifier
            .clip(shape)
            .background(BattleColors.surface)
            .let { if (wash != null) it.background(wash) else it }
            .border(1.dp, BattleColors.outline, shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // a turning light shows each of its lights in turn order
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            arena.lights.forEach { light -> LightBars(light, Modifier.size(width = 22.dp, height = 30.dp)) }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(arena.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = lightTextColor(first))
            Text(arena.description, color = BattleColors.textMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                stringResource(R.string.arena_slots, arena.loadoutSize),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            )
            arena.levelCap?.let { cap ->
                Spacer(Modifier.height(2.dp))
                Text(
                    stringResource(R.string.arena_level_cap, cap),
                    color = BattleColors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                )
            }
        }
    }
}
