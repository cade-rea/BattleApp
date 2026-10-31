package com.caderea.battleapp.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.caderea.battleapp.ui.menu.MainMenuScreen
import com.caderea.battleapp.ui.theme.BattleTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            BattleTheme {
                MainMenuScreen(
                    onBattle = { startActivity(Intent(this, BattleActivity::class.java)) },
                    onMap = { startActivity(Intent(this, MapActivity::class.java)) },
                )
            }
        }
    }
}
