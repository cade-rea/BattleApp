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
import com.caderea.battleapp.ui.character.CharactersScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.CharactersViewModel

/** Lists every saved character to switch, delete, or make a new one. */
class CharactersActivity : ComponentActivity() {

    private val viewModel: CharactersViewModel by viewModels {
        CharactersViewModel.factory(ProfileRepository.get(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BattleTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CharactersScreen(
                    state = state,
                    onSelect = { id ->
                        viewModel.select(id)
                        finish()
                    },
                    onDelete = viewModel::delete,
                    onNew = {
                        startActivity(Intent(this, CharacterCreationActivity::class.java))
                        finish()
                    },
                    onBack = ::finish,
                )
            }
        }
    }
}
