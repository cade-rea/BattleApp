package com.caderea.battleapp.ui.character

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.ui.battle.color
import com.caderea.battleapp.ui.fighter.AuraGlow
import com.caderea.battleapp.ui.fighter.FighterSummaryCard
import com.caderea.battleapp.ui.technique.TechniqueCard
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.ui.theme.readable
import com.caderea.battleapp.viewmodel.CharacterCreationUiState
import com.caderea.battleapp.viewmodel.CreationStep

/**
 * Character creation: a name and base color, then a signature move picked from three, then
 * two techniques picked from six, then the finished character.
 */
@Composable
fun CharacterCreationScreen(
    state: CharacterCreationUiState,
    onName: (String) -> Unit,
    onBase: (Channel) -> Unit,
    onStart: () -> Unit,
    onChooseSignature: (Int) -> Unit,
    onToggle: (Int) -> Unit,
    onKeep: () -> Unit,
    onStartOver: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BattleColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.create_title),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            if (state.step != CreationStep.READY) {
                TextButton(onBack) { Text(stringResource(R.string.button_cancel_creation), color = BattleColors.textMuted) }
            }
        }
        when (state.step) {
            CreationStep.IDENTITY -> Identity(state, onName, onBase, onStart)
            CreationStep.SIGNATURE -> Signature(state, onChooseSignature, onStartOver)
            CreationStep.TECHNIQUES -> Techniques(state, onToggle, onKeep, onStartOver)
            CreationStep.READY -> Ready(state, onDone)
        }
    }
}

@Composable
private fun Identity(
    state: CharacterCreationUiState,
    onName: (String) -> Unit,
    onBase: (Channel) -> Unit,
    onStart: () -> Unit,
) {
    OutlinedTextField(
        value = state.name,
        onValueChange = onName,
        modifier = Modifier.fillMaxWidth().testTag("name"),
        label = { Text(stringResource(R.string.create_name)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BattleColors.text,
            unfocusedBorderColor = BattleColors.outline,
            cursorColor = BattleColors.text,
        ),
    )
    Spacer(Modifier.height(4.dp))
    Text(stringResource(R.string.create_base), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text(stringResource(R.string.create_base_hint), color = BattleColors.textMuted, fontSize = 13.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Channel.entries.forEach { channel ->
            BaseChoice(channel, selected = state.base == channel, Modifier.weight(1f)) { onBase(channel) }
        }
    }
    Spacer(Modifier.height(8.dp))
    Text(
        stringResource(R.string.create_draft_hint, state.picks),
        color = BattleColors.textMuted,
        fontSize = 13.sp,
    )
    Button(onStart, Modifier.fillMaxWidth().height(52.dp).testTag("start"), enabled = state.canStart) {
        Text(stringResource(R.string.button_roll_techniques), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** One primary to start as: its orb, its name, what it's good at and what it beats. */
@Composable
private fun BaseChoice(channel: Channel, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val color = channel.color.readable
    Column(
        modifier
            .clip(shape)
            .background(if (selected) BattleColors.surfaceRaised else BattleColors.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) color else BattleColors.outline, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 8.dp)
            .testTag("base${channel.name}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AuraGlow(channel.color, size = 52.dp)
        Text(stringResource(channel.nameRes), color = color, fontWeight = FontWeight.Bold)
        Text(
            stringResource(channel.traitRes),
            color = BattleColors.text,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.create_beats, stringResource(channel.prey.nameRes)),
            color = BattleColors.textMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ColumnScope.Signature(state: CharacterCreationUiState, onChoose: (Int) -> Unit, onStartOver: () -> Unit) {
    state.fighter?.let { FighterSummaryCard(it, Modifier.fillMaxWidth()) }
    StepDots(step = 1, steps = 2)
    Text(stringResource(R.string.create_signature), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    Text(
        stringResource(R.string.create_signature_hint, stringResource(state.base!!.nameRes)),
        color = BattleColors.textMuted,
        fontSize = 13.sp,
    )
    state.offer.forEachIndexed { index, technique ->
        TechniqueCard(
            technique,
            Modifier.fillMaxWidth().testTag("offer$index"),
            onClick = { onChoose(index) },
            showSlot = false,
        )
    }
    TextButton(onStartOver, Modifier.align(Alignment.Start)) {
        Text(stringResource(R.string.button_start_over), color = BattleColors.textMuted)
    }
}

@Composable
private fun ColumnScope.Techniques(
    state: CharacterCreationUiState,
    onToggle: (Int) -> Unit,
    onKeep: () -> Unit,
    onStartOver: () -> Unit,
) {
    state.fighter?.let { FighterSummaryCard(it, Modifier.fillMaxWidth()) }
    StepDots(step = 2, steps = 2)
    Text(stringResource(R.string.create_kept), color = BattleColors.textMuted, fontSize = 13.sp)
    state.kept.forEach { TechniqueCard(it, Modifier.fillMaxWidth()) }
    Spacer(Modifier.height(4.dp))
    Text(
        stringResource(R.string.create_techniques, state.picks, state.offer.size),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Text(stringResource(R.string.create_techniques_hint), color = BattleColors.textMuted, fontSize = 13.sp)
    // marked offers show the button they'll get, after the signature's
    val order = state.selected.sorted()
    state.offer.forEachIndexed { index, technique ->
        val slot = order.indexOf(index).takeIf { it >= 0 }?.let { it + state.kept.size }
        TechniqueCard(
            technique.copy(slot = slot),
            Modifier.fillMaxWidth().testTag("offer$index"),
            onClick = { onToggle(index) },
        )
    }
    if (state.needsAttack) {
        Text(stringResource(R.string.create_needs_attack), color = BattleColors.text, fontSize = 13.sp)
    }
    Button(onKeep, Modifier.fillMaxWidth().height(52.dp).testTag("keep"), enabled = state.canKeep) {
        Text(stringResource(R.string.button_keep_picks, state.picks), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
    TextButton(onStartOver, Modifier.align(Alignment.Start)) {
        Text(stringResource(R.string.button_start_over), color = BattleColors.textMuted)
    }
}

/** One bar per step: bright for steps done, grey for the current one. */
@Composable
private fun StepDots(step: Int, steps: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(steps) { index ->
            val done = index + 1 < step
            val current = index + 1 == step
            Box(
                Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        when {
                            done -> BattleColors.text
                            current -> BattleColors.textMuted
                            else -> BattleColors.surfaceRaised
                        }
                    )
            )
        }
    }
}

@Composable
private fun Ready(state: CharacterCreationUiState, onDone: () -> Unit) {
    val fighter = state.fighter ?: return
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        AuraGlow(fighter.color, size = 96.dp)
        Spacer(Modifier.height(8.dp))
        Text(fighter.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.create_ready), color = BattleColors.textMuted)
    }
    state.kept.forEach { TechniqueCard(it, Modifier.fillMaxWidth()) }
    Spacer(Modifier.height(4.dp))
    Button(onDone, Modifier.fillMaxWidth().height(52.dp).testTag("done")) {
        Text(stringResource(R.string.button_to_menu), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

val Channel.nameRes: Int
    get() = when (this) {
        Channel.RED -> R.string.channel_red
        Channel.GREEN -> R.string.channel_green
        Channel.BLUE -> R.string.channel_blue
    }

private val Channel.traitRes: Int
    get() = when (this) {
        Channel.RED -> R.string.trait_red
        Channel.GREEN -> R.string.trait_green
        Channel.BLUE -> R.string.trait_blue
    }
