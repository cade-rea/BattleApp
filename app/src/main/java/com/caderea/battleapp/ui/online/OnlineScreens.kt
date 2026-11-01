package com.caderea.battleapp.ui.online

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caderea.battleapp.R
import com.caderea.battleapp.online.ServerAddress
import com.caderea.battleapp.ui.battle.BattleScreen
import com.caderea.battleapp.ui.fighter.FighterSummaryCard
import com.caderea.battleapp.ui.theme.BattleColors
import com.caderea.battleapp.viewmodel.FighterSummary
import com.caderea.battleapp.viewmodel.OnlinePhase
import com.caderea.battleapp.viewmodel.OnlineUiState

/**
 * Before going online: which server to play on. Until the server is hosted, it runs on
 * someone's computer, so its address is typed in and remembered.
 */
@Composable
fun OnlineSetupScreen(
    fighter: FighterSummary?,
    server: String,
    onContinue: (url: String) -> Unit,
    onBack: () -> Unit,
) {
    var address by rememberSaveable { mutableStateOf(server) }
    val url = ServerAddress.normalize(address)

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
                stringResource(R.string.online_title),
                Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            TextButton(onBack) { Text(stringResource(R.string.button_back), color = BattleColors.textMuted) }
        }
        fighter?.let { FighterSummaryCard(it, Modifier.fillMaxWidth()) }
        Text(stringResource(R.string.online_intro), color = BattleColors.textMuted, fontSize = 13.sp)
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("server"),
            label = { Text(stringResource(R.string.online_server_label)) },
            singleLine = true,
            isError = url == null,
            supportingText = {
                Text(stringResource(if (url == null) R.string.online_server_invalid else R.string.online_server_hint))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { url?.let(onContinue) },
            enabled = url != null && fighter != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("continue"),
        ) {
            Text(stringResource(R.string.button_choose_arena), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * An online match from start to end: connecting, waiting for an opponent, the battle itself
 * (the same screen as an offline one), or what went wrong.
 */
@Composable
fun OnlineBattleScreen(
    state: OnlineUiState,
    onAction: (slot: Int) -> Unit,
    onCancel: () -> Unit,
    onGiveUp: () -> Unit,
    onAgain: () -> Unit,
    onExit: () -> Unit,
) {
    when (state.phase) {
        OnlinePhase.PLAYING, OnlinePhase.OVER -> BattleScreen(
            state = state.battle,
            tickLengthMs = state.tickLengthMs,
            onAction = onAction,
            onCancel = onCancel,
            onTogglePause = {},
            onGiveUp = onGiveUp,
            onRematch = onAgain,
            onExit = onExit,
        )
        OnlinePhase.CONNECTING, OnlinePhase.WAITING -> Status(
            title = if (state.phase == OnlinePhase.CONNECTING) {
                stringResource(R.string.online_connecting, state.server)
            } else {
                stringResource(R.string.online_waiting, state.arenaName)
            },
            detail = if (state.phase == OnlinePhase.WAITING) stringResource(R.string.online_waiting_hint) else null,
            busy = true,
        ) {
            OutlinedButton(onExit, Modifier.testTag("stop_looking")) { Text(stringResource(R.string.button_stop_looking)) }
        }
        OnlinePhase.FAILED -> Status(
            title = stringResource(R.string.online_failed),
            detail = state.problem ?: stringResource(R.string.online_lost_connection),
            busy = false,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onExit) { Text(stringResource(R.string.button_back)) }
                Button(onAgain, Modifier.testTag("try_again")) { Text(stringResource(R.string.button_try_again)) }
            }
        }
    }
}

@Composable
private fun Status(title: String, detail: String?, busy: Boolean, actions: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BattleColors.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(32.dp)
            .testTag("online_status"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (busy) {
            CircularProgressIndicator(Modifier.size(40.dp), color = BattleColors.text)
            Spacer(Modifier.height(24.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        detail?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = BattleColors.textMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(28.dp))
        actions()
    }
}
