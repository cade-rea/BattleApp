package com.caderea.battleapp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.R
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.ui.battle.BattleScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.BattleViewModel

/**
 * Battle screen. It forwards taps to [BattleViewModel] and draws whatever state the
 * ViewModel publishes; the battle itself lives in the ViewModel.
 */
class BattleActivity : ComponentActivity() {

    private val tickLengthMs: Long
        get() = resources.getInteger(R.integer.tick_length_ms).toLong()

    private val viewModel: BattleViewModel by viewModels { BattleViewModel.factory(tickLengthMs, ProfileRepository.get(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BattleTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                BattleScreen(
                    state = state,
                    tickLengthMs = tickLengthMs,
                    onAction = viewModel::queueAction,
                    onCancel = viewModel::cancelLast,
                    onTogglePause = viewModel::togglePause,
                    onGiveUp = viewModel::stop,
                    onRematch = viewModel::rematch,
                    onExit = ::finish,
                )
            }
        }
    }
}
