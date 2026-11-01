package com.caderea.battleapp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.online.OkHttpConnection
import com.caderea.battleapp.online.ServerAddress
import com.caderea.battleapp.ui.online.OnlineBattleScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.OnlineBattleViewModel

/**
 * An online match. Like [BattleActivity] it only forwards taps and draws; the match lives on
 * the server and [OnlineBattleViewModel] keeps the connection. Leaving the screen hangs up.
 */
class OnlineBattleActivity : ComponentActivity() {

    private val viewModel: OnlineBattleViewModel by viewModels {
        OnlineBattleViewModel.factory(
            OkHttpConnection.connector,
            OnlineActivity.serverOf(intent) ?: ServerAddress.DEFAULT,
            ProfileRepository.get(this).requireActive(),
            ArenasActivity.arenaOf(intent) ?: Arenas.DEFAULT,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BattleTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                OnlineBattleScreen(
                    state = state,
                    onAction = viewModel::queueAction,
                    onCancel = viewModel::cancelLast,
                    onGiveUp = viewModel::giveUp,
                    onAgain = viewModel::again,
                    onExit = ::finish,
                )
            }
        }
    }
}
