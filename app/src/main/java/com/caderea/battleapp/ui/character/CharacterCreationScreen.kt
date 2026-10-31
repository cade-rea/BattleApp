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
 * Character creation: a name and base color, then four rounds of picking one technique out
 * of three, the last round being the signature move, then the finished character.
 */
@Composable
fun CharacterCreationScreen(
    state: CharacterCreationUiState,
    onName: (String) -> Unit,
    onBase: (Channel) -> Unit,
    onStart: () -> Unit,
    onPick: (Int) -> Unit,
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
            CreationStep.DRAFT -> Draft(state, onPick, onStartOver)
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
        stringResource(R.string.create_draft_hint, state.rounds),
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
private fun ColumnScope.Draft(state: CharacterCreationUiState, onPick: (Int) -> Unit, onStartOver: () -> Unit) {
    state.fighter?.let { FighterSummaryCard(it, Modifier.fillMaxWidth()) }
    RoundDots(state.round, state.rounds)
    Text(
        if (state.signatureRound) {
            stringResource(R.string.create_round_signature)
        } else {
            stringResource(R.string.create_round, state.round, state.rounds)
        },
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
    Text(
        if (state.signatureRound) {
            stringResource(R.string.create_round_signature_hint, stringResource(state.base!!.nameRes))
        } else {
            stringResource(R.string.create_round_hint)
        },
        color = BattleColors.textMuted,
        fontSize = 13.sp,
    )
    state.offer.forEachIndexed { index, technique ->
        TechniqueCard(
            technique,
            Modifier.fillMaxWidth().testTag("offer$index"),
            onClick = { onPick(index) },
            showSlot = false,
        )
    }
    if (state.picks.isNotEmpty()) {
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.create_picked), color = BattleColors.textMuted, fontSize = 13.sp)
        state.picks.forEach { TechniqueCard(it, Modifier.fillMaxWidth()) }
    }
    TextButton(onStartOver, Modifier.align(Alignment.Start)) {
        Text(stringResource(R.string.button_start_over), color = BattleColors.textMuted)
    }
}

/** One dot per round: filled for rounds picked, ringed for the current one. */
@Composable
private fun RoundDots(round: Int, rounds: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(rounds) { index ->
            val done = index + 1 < round
            val current = index + 1 == round
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
    state.picks.forEach { TechniqueCard(it, Modifier.fillMaxWidth()) }
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
