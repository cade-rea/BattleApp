package com.caderea.battleapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.core.environment.Arena
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.ui.arena.ArenasScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.toCard
import com.caderea.battleapp.viewmodel.toSummary

/** Picks where to fight, then goes on to the loadout for that arena. */
class ArenasActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val profiles = ProfileRepository.get(this)
        val arenas = Arenas.ALL.map { it.toCard() }

        setContent {
            BattleTheme {
                val profile by profiles.active.collectAsStateWithLifecycle()
                ArenasScreen(
                    fighter = profile?.toSummary(),
                    arenas = arenas,
                    onPick = { id ->
                        startActivity(withArena(Intent(this, LoadoutActivity::class.java), Arenas.byId(id)))
                    },
                    onBack = ::finish,
                )
            }
        }
    }

    companion object {
        private const val EXTRA_ARENA = "arena"

        fun withArena(intent: Intent, arena: Arena): Intent = intent.putExtra(EXTRA_ARENA, arena.id)

        /** The arena an intent was started for, or null if none was picked. */
        fun arenaOf(intent: Intent): Arena? = intent.getStringExtra(EXTRA_ARENA)?.let(Arenas::byId)

        fun start(context: Context) = context.startActivity(Intent(context, ArenasActivity::class.java))
    }
}
