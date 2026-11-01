package com.caderea.battleapp.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.R
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.online.ServerAddress
import com.caderea.battleapp.ui.online.OnlineSetupScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.toSummary

/** Picks the server to play on, then goes on to the arenas for an online match. */
class OnlineActivity : ComponentActivity() {

    // the address to go on with once the local network permission is answered
    private var pendingUrl: String? = null

    private val askForLocalNetwork = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // a hosted server doesn't need the permission, so go on either way
        if (!granted) Toast.makeText(this, R.string.online_local_network_denied, Toast.LENGTH_LONG).show()
        pendingUrl?.let(::chooseArena)
        pendingUrl = null
    }

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
                        if (needsLocalNetworkPermission()) {
                            pendingUrl = url
                            askForLocalNetwork.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                        } else {
                            chooseArena(url)
                        }
                    },
                    onBack = ::finish,
                )
            }
        }
    }

    private fun chooseArena(url: String) {
        startActivity(online(Intent(this, ArenasActivity::class.java), url))
    }

    // apps targeting Android 17 can't reach the local network until the player allows it
    private fun needsLocalNetworkPermission(): Boolean =
        Build.VERSION.SDK_INT >= ANDROID_17 &&
            checkSelfPermission(Manifest.permission.ACCESS_LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED

    companion object {
        private const val ANDROID_17 = 37
        private const val EXTRA_SERVER = "server"

        /** Marks an intent as part of an online match on the server at [url]. */
        fun online(intent: Intent, url: String): Intent = intent.putExtra(EXTRA_SERVER, url)

        /** The server an intent is playing on, or null for a battle against the computer. */
        fun serverOf(intent: Intent): String? = intent.getStringExtra(EXTRA_SERVER)

        fun start(context: Context) = context.startActivity(Intent(context, OnlineActivity::class.java))
    }
}
