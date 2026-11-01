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

/**
 * Picks the techniques the player brings into battle: for the arena it was started with,
 * or in general from the menu.
 */
class LoadoutActivity : ComponentActivity() {

    private val arena by lazy { ArenasActivity.arenaOf(intent) }

    private val viewModel: LoadoutViewModel by viewModels {
        LoadoutViewModel.factory(ProfileRepository.get(this), arena)
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
                        val arena = arena
                        val server = OnlineActivity.serverOf(intent)
                        if (arena != null && server != null) {
                            val battle = Intent(this, OnlineBattleActivity::class.java)
                            startActivity(OnlineActivity.online(ArenasActivity.withArena(battle, arena), server))
                        } else if (arena != null) {
                            startActivity(ArenasActivity.withArena(Intent(this, BattleActivity::class.java), arena))
                        } else {
                            ArenasActivity.start(this)
                        }
                        finish()
                    },
                    onBack = ::finish,
                )
            }
        }
    }
}
