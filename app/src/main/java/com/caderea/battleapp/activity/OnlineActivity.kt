package com.caderea.battleapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.online.ServerAddress
import com.caderea.battleapp.ui.online.OnlineSetupScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.toSummary

/** Picks the server to play on, then goes on to the arenas for an online match. */
class OnlineActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val profiles = ProfileRepository.get(this)

        setContent {
            BattleTheme {
                val profile by profiles.active.collectAsStateWithLifecycle()
                OnlineSetupScreen(
                    fighter = profile?.toSummary(),
                    server = ServerAddress.load(this),
                    onContinue = { url ->
                        ServerAddress.save(this, url)
                        startActivity(online(Intent(this, ArenasActivity::class.java), url))
                    },
                    onBack = ::finish,
                )
            }
        }
    }

    companion object {
        private const val EXTRA_SERVER = "server"

        /** Marks an intent as part of an online match on the server at [url]. */
        fun online(intent: Intent, url: String): Intent = intent.putExtra(EXTRA_SERVER, url)

        /** The server an intent is playing on, or null for a battle against the computer. */
        fun serverOf(intent: Intent): String? = intent.getStringExtra(EXTRA_SERVER)

        fun start(context: Context) = context.startActivity(Intent(context, OnlineActivity::class.java))
    }
}
