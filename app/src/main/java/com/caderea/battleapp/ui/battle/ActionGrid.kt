package com.caderea.battleapp.ui.battle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.ui.technique.effectText
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.ActionButtonState
import com.caderea.battleapp.viewmodel.ActionKind

/**
 * The player's techniques, two to a row, for the four to six slots an arena allows.
 */
@Composable
fun ActionGrid(actions: List<ActionButtonState>, onAction: (slot: Int) -> Unit, modifier: Modifier = Modifier) {
    // a third row of buttons would squeeze out the type triangle, so more than four get shorter
    val height = if (actions.size > 4) COMPACT_CARD_HEIGHT else CARD_HEIGHT
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actions.withIndex().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (slot, action) ->
                    ActionCard(
                        action,
                        height,
                        onClick = { onAction(slot) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action$slot"),
                    )
                }
                // an odd last button keeps its width
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/**
 * One technique: its color along the edge, then what it costs, how long it winds up, and
 * what it would do right now, including the type matchup against the enemy.
 */
@Composable
private fun ActionCard(action: ActionButtonState, height: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    val color = action.color.readable

    if (action.name.isEmpty()) {
        Box(modifier.height(height).clip(shape).background(BattleColors.surface))
        return
    }

    Row(
        modifier
            .height(height)
            .alpha(if (action.enabled) 1f else 0.38f)
            .clip(shape)
            .background(BattleColors.surface)
            .border(1.dp, color.copy(alpha = 0.5f), shape)
            .clickable(enabled = action.enabled, onClick = onClick)
    ) {
        Box(
            Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(action.color.compose)
        )
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KindGlyph(action.kind, color, size = 14.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    action.name,
                    Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.action_cost, action.cost),
                    color = BattleColors.energy,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    effectText(action.kind, action.effect),
                    color = BattleColors.text,
                    fontSize = 13.sp,
                )
                if (action.kind == ActionKind.ATTACK) {
                    Spacer(Modifier.width(6.dp))
                    MatchupBadge(action.effectiveness)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    pluralStringResource(R.plurals.wind_up_ticks, action.windUpTicks, action.windUpTicks),
                    color = BattleColors.textMuted,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

/**
 * The type matchup as arrows: up when the attack's color beats the enemy's aura, down when
 * it's beaten. It stays white, since red and green here would read as types.
 */
@Composable
fun MatchupBadge(effectiveness: Double, modifier: Modifier = Modifier) {
    val (arrow, emphasis) = when {
        effectiveness >= 1.05 -> "▲" to BattleColors.text
        effectiveness <= 0.95 -> "▼" to BattleColors.textMuted
        else -> "" to BattleColors.textMuted
    }
    Text(
        stringResource(R.string.matchup, arrow, effectiveness),
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BattleColors.surfaceRaised)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        color = emphasis,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        fontWeight = if (effectiveness >= 1.05) FontWeight.Bold else FontWeight.Normal,
    )
}

private val CARD_HEIGHT = 76.dp
private val COMPACT_CARD_HEIGHT = 60.dp
