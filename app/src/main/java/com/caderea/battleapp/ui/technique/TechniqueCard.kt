package com.caderea.battleapp.ui.technique

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.ui.battle.KindGlyph
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.compose
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.ActionKind
import com.caderea.battleapp.viewmodel.TechniqueCardState

/**
 * A technique from the catalog: its color along the edge, then its stats against a neutral
 * opponent and its own potency roll. A technique in the loadout shows its button number.
 */
@Composable
fun TechniqueCard(technique: TechniqueCardState, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val shape = RoundedCornerShape(14.dp)
    val color = technique.color.readable
    val chosen = technique.slot != null

    Row(
        modifier
            .height(64.dp)
            .clip(shape)
            .background(if (chosen) BattleColors.surfaceRaised else BattleColors.surface)
            .border(if (chosen) 2.dp else 1.dp, color.copy(alpha = if (chosen) 0.9f else 0.35f), shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        Box(
            Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(technique.color.compose)
        )
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KindGlyph(technique.kind, color, size = 14.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    technique.name,
                    Modifier.weight(1f, fill = false),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    technique.color.hex,
                    color = color,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    stringResource(R.string.action_cost, technique.cost),
                    color = BattleColors.energy,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(effectText(technique.kind, technique.effect), fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                if (technique.potencyPercent != 0) {
                    Text(
                        stringResource(R.string.technique_potency, technique.potencyPercent),
                        color = if (technique.potencyPercent > 0) BattleColors.text else BattleColors.textMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    pluralStringResource(R.plurals.wind_up_ticks, technique.windUpTicks, technique.windUpTicks),
                    color = BattleColors.textMuted,
                    fontSize = 12.sp,
                )
            }
        }
        if (onClick != null) {
            SlotBadge(technique.slot, Modifier.align(Alignment.CenterVertically).padding(end = 12.dp))
        }
    }
}

/** The technique's button number in a filled circle, or an empty ring if it stays home. */
@Composable
private fun SlotBadge(slot: Int?, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (slot != null) BattleColors.text else BattleColors.background)
            .border(1.dp, BattleColors.outline, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (slot != null) {
            Text(
                "${slot + 1}",
                color = BattleColors.background,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun effectText(kind: ActionKind, effect: Int): String = when (kind) {
    ActionKind.ATTACK -> stringResource(R.string.effect_damage, effect)
    ActionKind.HEAL -> stringResource(R.string.effect_heal, effect)
    ActionKind.GUARD -> stringResource(R.string.effect_block, effect)
}
