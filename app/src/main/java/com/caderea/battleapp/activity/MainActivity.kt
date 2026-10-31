package com.caderea.battleapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.ui.menu.MainMenuScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.toSummary

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val profiles = ProfileRepository.get(this)

        setContent {
            BattleTheme {
                val profile by profiles.profile.collectAsStateWithLifecycle()
                MainMenuScreen(
                    fighter = profile.toSummary(),
                    onBattle = { startActivity(Intent(this, BattleActivity::class.java)) },
                    onLoadout = { startActivity(Intent(this, LoadoutActivity::class.java)) },
                    onMap = { startActivity(Intent(this, MapActivity::class.java)) },
                )
            }
        }
    }
}
