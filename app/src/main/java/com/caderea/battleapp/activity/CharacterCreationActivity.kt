package com.caderea.battleapp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.ui.character.CharacterCreationScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.CharacterCreationViewModel

/** Makes a new character, which is loaded once it's done. */
class CharacterCreationActivity : ComponentActivity() {

    private val viewModel: CharacterCreationViewModel by viewModels {
        CharacterCreationViewModel.factory(ProfileRepository.get(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BattleTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CharacterCreationScreen(
                    state = state,
                    onName = viewModel::setName,
                    onBase = viewModel::setBase,
                    onStart = viewModel::startDraft,
                    onChooseSignature = viewModel::chooseSignature,
                    onToggle = viewModel::toggle,
                    onKeep = viewModel::keep,
                    onStartOver = viewModel::startOver,
                    onDone = ::finish,
                    onBack = ::finish,
                )
            }
        }
    }
}
