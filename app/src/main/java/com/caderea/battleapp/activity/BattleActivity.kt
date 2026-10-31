package com.caderea.battleapp.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.caderea.battleapp.R
import com.caderea.battleapp.view.battle.BattleViewGroup
import com.caderea.battleapp.viewmodel.BattleUiState
import com.caderea.battleapp.viewmodel.BattleViewModel
import com.caderea.battleapp.viewmodel.FighterState
import kotlinx.coroutines.launch

/**
 * Battle screen. It forwards button presses to [BattleViewModel] and draws whatever state the
 * ViewModel publishes; the battle itself lives in the ViewModel.
 */
class BattleActivity : ComponentActivity() {

    private val viewModel: BattleViewModel by viewModels {
        BattleViewModel.factory(resources.getInteger(R.integer.tick_length_ms).toLong())
    }

    private lateinit var battleButtons: List<Button>
    private lateinit var stopButton: Button
    private lateinit var battleViewGroup: BattleViewGroup
    private lateinit var battleStatus: TextView
    private lateinit var battleLog: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle_screen)
        padContentForSystemBars()

        battleButtons = listOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4)
            .map { findViewById<Button>(it) }
        battleButtons.forEachIndexed { slot, button ->
            button.setOnClickListener { viewModel.queueAction(slot) }
        }

        stopButton = findViewById(R.id.buttonStop)
        stopButton.setOnClickListener { viewModel.stop() }
        findViewById<Button>(R.id.buttonCancel).setOnClickListener { viewModel.cancelLast() }

        battleViewGroup = findViewById(R.id.battleViewGroup)
        battleStatus = findViewById(R.id.battleStatus)
        battleLog = findViewById(R.id.battleLog)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: BattleUiState) {
        battleButtons.forEachIndexed { slot, button ->
            val action = state.actions.getOrNull(slot)
            button.text = when {
                action == null || action.name.isEmpty() -> ""
                else -> getString(R.string.action_button, action.name, action.cost)
            }
            button.isEnabled = action?.enabled == true
        }

        battleViewGroup.showQueues(state.playerQueue, state.enemyQueue)
        battleViewGroup.updateTickProgress(state.tickProgress)
        battleStatus.text = listOf(state.player, state.enemy).joinToString("\n", transform = ::statusLine)
        battleLog.text = state.log.joinToString("\n")

        if (state.isOver) {
            //change the stop button to return to the main menu
            stopButton.setText(R.string.button_return)
            stopButton.setOnClickListener { finish() }
        }
    }

    private fun statusLine(fighter: FighterState): String {
        val status = getString(
            R.string.fighter_status,
            fighter.name, fighter.health, fighter.maxHealth, fighter.energy, fighter.maxEnergy,
        )
        return fighter.guard?.let { "$status · ${getString(R.string.fighter_guard, it)}" } ?: status
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.battle_screen, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
