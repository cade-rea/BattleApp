package com.caderea.battleapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.ui.loadout.LoadoutScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.LoadoutViewModel

/** Picks the techniques the player brings into battle. */
class LoadoutActivity : ComponentActivity() {

    private val viewModel: LoadoutViewModel by viewModels {
        LoadoutViewModel.factory(ProfileRepository.get(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BattleTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                LoadoutScreen(
                    state = state,
                    onToggle = viewModel::toggle,
                    onBattle = {
                        startActivity(Intent(this, BattleActivity::class.java))
                        finish()
                    },
                    onBack = ::finish,
                )
            }
        }
    }
}
