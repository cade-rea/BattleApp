package com.caderea.battleapp.ui.battle

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.ui.arena.LightBars
import com.caderea.battleapp.ui.arena.lightLabel
import com.caderea.battleapp.ui.arena.lightTextColor
import com.caderea.battleapp.ui.arena.lightWash
import com.caderea.battleapp.ui.fighter.AuraGlow
import com.caderea.battleapp.ui.fighter.LevelBar
import com.caderea.battleapp.ui.technique.TechniqueCard
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.viewmodel.ArenaHudState
import com.caderea.battleapp.viewmodel.BattleUiState
import com.caderea.battleapp.viewmodel.BattleViewModel
import com.caderea.battleapp.viewmodel.Outcome
import com.caderea.battleapp.viewmodel.RewardsState

/**
 * The battle screen, drawn from [state] alone: the enemy on top, the shared timeline, the
 * type triangle, then the player and their techniques, so the eye moves from what's coming
 * to what to answer it with. Whatever lands on a tick plays out over the top of it all.
 *
 * @param tickLengthMs how long a tick lasts, so the tick bar and timeline glide in step
 */
@Composable
fun BattleScreen(
    state: BattleUiState,
    tickLengthMs: Long,
    onAction: (slot: Int) -> Unit,
    onCancel: () -> Unit,
    onTogglePause: () -> Unit,
    onGiveUp: () -> Unit,
    onRematch: () -> Unit,
    onExit: () -> Unit,
) {
    val tickFraction = rememberTickFraction(state, tickLengthMs)
    val anchors = remember { EffectAnchors() }

    val wash = state.arena?.let { lightWash(it.light) }
    Box(
        Modifier
            .fillMaxSize()
            .background(BattleColors.background)
            .let { if (wash != null) it.background(wash) else it }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TopBar(state, tickFraction, onTogglePause, onGiveUp)
            state.arena?.let { ArenaStrip(it, Modifier.fillMaxWidth()) }
            FighterPanel(state.enemy, Modifier.fillMaxWidth(), onOrbPositioned = { anchors.enemyOrb = it })
            Timeline(
                state.enemy,
                state.enemyQueue,
                state.player,
                state.playerQueue,
                tickFraction,
                Modifier.fillMaxWidth(),
                onLaneAnchors = { enemy, player ->
                    anchors.enemyLane = enemy
                    anchors.playerLane = player
                },
            )
            // the type triangle fills whatever room is left between the queues and the player
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                ColorTriangle(
                    state.player.color,
                    state.enemy.color,
                    youLabel = stringResource(R.string.triangle_you),
                    foeLabel = stringResource(R.string.triangle_foe),
                    modifier = Modifier
                        .fillMaxHeight()
                        .heightIn(max = 170.dp)
                        .aspectRatio(1.2f),
                )
            }
            FighterPanel(state.player, Modifier.fillMaxWidth(), onOrbPositioned = { anchors.playerOrb = it })
            ActionGrid(state.actions, onAction, Modifier.fillMaxWidth())
            OutlinedButton(
                onClick = onCancel,
                enabled = !state.isOver && state.playerQueue.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cancel"),
            ) {
                Text(stringResource(R.string.button_cancel))
            }
        }

        ResolveEffects(
            state,
            anchors,
            durationMs = (tickLengthMs * 0.85).toInt(),
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(state.outcome != null, enter = fadeIn(), exit = fadeOut()) {
            state.outcome?.let { OutcomeOverlay(it, state.tick, state.rewards, onRematch, onExit) }
        }
    }
}

/**
 * How far through the current tick we are, from 0 to 1, animated smoothly between the
 * ViewModel's sub-tick updates. It holds still while paused.
 */
@Composable
private fun rememberTickFraction(state: BattleUiState, tickLengthMs: Long): Float {
    val fraction = remember { Animatable(0f) }
    val subtickMs = (tickLengthMs / BattleViewModel.SUBTICKS_PER_TICK).toInt()

    LaunchedEffect(state.tick, state.tickProgress, state.isPaused, state.isOver) {
        val start = state.tickProgress / 100f
        // a new tick starts the bar over; otherwise it only catches up, never runs backwards
        if (state.tickProgress == 0 || fraction.value < start) fraction.snapTo(start)
        if (state.isPaused || state.isOver) return@LaunchedEffect

        fraction.animateTo(
            start + 1f / BattleViewModel.SUBTICKS_PER_TICK,
            tween(subtickMs, easing = LinearEasing),
        )
    }
    return fraction.value
}

/**
 * The arena and its light right now, and for a turning light, what comes next and when,
 * so a big hit can be timed for it.
 */
@Composable
private fun ArenaStrip(arena: ArenaHudState, modifier: Modifier = Modifier) {
    Row(modifier.testTag("arena"), verticalAlignment = Alignment.CenterVertically) {
        LightBars(arena.light, Modifier.size(width = 16.dp, height = 18.dp))
        Spacer(Modifier.width(8.dp))
        Text(arena.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(8.dp))
        Text(lightLabel(arena.light), color = lightTextColor(arena.light), fontSize = 12.sp)
        Spacer(Modifier.weight(1f))
        val next = arena.nextLight
        val ticks = arena.ticksLeftInLight
        if (next != null && ticks != null) {
            Text(
                stringResource(R.string.light_next, lightLabel(next), ticks),
                color = lightTextColor(next),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
            )
        } else {
            arena.levelCap?.let { cap ->
                Text(
                    stringResource(R.string.arena_level_cap, cap),
                    color = BattleColors.textMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun TopBar(state: BattleUiState, tickFraction: Float, onTogglePause: () -> Unit, onGiveUp: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(R.string.tick_count, state.tick),
            Modifier.width(72.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
        Canvas(
            Modifier
                .weight(1f)
                .height(4.dp)
        ) {
            val corner = CornerRadius(size.height / 2)
            drawRoundRect(BattleColors.surfaceRaised, cornerRadius = corner)
            drawRoundRect(
                if (state.isPaused) BattleColors.textMuted else BattleColors.text,
                size = Size(size.width * tickFraction, size.height),
                cornerRadius = corner,
            )
        }
        Spacer(Modifier.width(4.dp))
        TextButton(onTogglePause, enabled = !state.isOver, modifier = Modifier.testTag("pause")) {
            Text(stringResource(if (state.isPaused) R.string.button_resume else R.string.button_pause))
        }
        TextButton(onGiveUp, enabled = !state.isOver) {
            Text(stringResource(R.string.button_give_up), color = BattleColors.textMuted)
        }
    }
}

@Composable
private fun OutcomeOverlay(
    outcome: Outcome,
    ticks: Int,
    rewards: RewardsState?,
    onRematch: () -> Unit,
    onExit: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f))
            // swallows taps so the battle underneath can't be touched
            .clickable(remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(32.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(BattleColors.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(
                    when (outcome) {
                        Outcome.WON -> R.string.outcome_won
                        Outcome.LOST -> R.string.outcome_lost
                        Outcome.STOPPED -> R.string.outcome_stopped
                    }
                ),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.outcome_ticks, ticks), color = BattleColors.textMuted)
            rewards?.let {
                Spacer(Modifier.height(20.dp))
                Rewards(it)
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onExit) { Text(stringResource(R.string.button_menu)) }
                Button(onRematch, Modifier.testTag("rematch")) { Text(stringResource(R.string.button_rematch)) }
            }
        }
    }
}

/**
 * What the battle earned: XP, the aura before and after (it shifts toward the colors used),
 * the level, and any new technique.
 */
@Composable
private fun Rewards(rewards: RewardsState) {
    Column(Modifier.testTag("rewards"), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraGlow(rewards.auraBefore, size = 28.dp)
            Text("→", Modifier.padding(horizontal = 4.dp), color = BattleColors.textMuted)
            AuraGlow(rewards.auraAfter, size = 36.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    stringResource(R.string.rewards_xp, rewards.xp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    if (rewards.levelsGained > 0) {
                        stringResource(R.string.rewards_level_up) + " " + stringResource(R.string.rewards_level, rewards.level)
                    } else {
                        stringResource(R.string.rewards_level, rewards.level)
                    },
                    color = if (rewards.levelsGained > 0) BattleColors.text else BattleColors.textMuted,
                    fontSize = 13.sp,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        LevelBar(rewards.levelProgress, rewards.auraAfter, Modifier.width(200.dp))
        rewards.newTechnique?.let { technique ->
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.rewards_new_technique), color = BattleColors.textMuted, fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            TechniqueCard(technique, Modifier.fillMaxWidth())
        }
    }
}
