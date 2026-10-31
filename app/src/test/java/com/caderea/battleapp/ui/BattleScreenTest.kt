package com.caderea.battleapp.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.fighter.EnemyFighter
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.progression.Progression
import com.caderea.battleapp.core.technique.TechniqueRoller
import com.caderea.battleapp.data.ProfileRepository
import com.caderea.battleapp.ui.battle.BattleScreen
import com.caderea.battleapp.ui.loadout.LoadoutScreen
import com.caderea.battleapp.viewmodel.LoadoutViewModel
import com.caderea.battleapp.viewmodel.toState
import com.caderea.battleapp.viewmodel.toSummary
import com.caderea.battleapp.ui.menu.MainMenuScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.BattleUiState
import com.caderea.battleapp.viewmodel.BattleViewModel
import com.caderea.battleapp.viewmodel.Outcome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.random.Random

/**
 * Renders the screens on the JVM with Robolectric. Besides checking the controls, each
 * test saves a screenshot to app/build/screenshots, so a change to the look can be
 * reviewed without an emulator.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w400dp-h880dp-xhdpi")
class BattleScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun tappingAnActionQueuesItsSlot() {
        val tapped = mutableListOf<Int>()
        show(midBattle(), onAction = { tapped += it })

        composeRule.onNodeWithTag("action2").performClick()

        assertEquals(listOf(2), tapped)
        screenshot("battle")
    }

    @Test
    fun actionsYouCantAffordAreDisabled() {
        val state = midBattle()
        show(state)

        state.actions.forEachIndexed { slot, action ->
            val node = composeRule.onNodeWithTag("action$slot")
            if (action.enabled) node.assertIsEnabled() else node.assertIsNotEnabled()
        }
    }

    @Test
    fun landedActionsPlayOut() {
        composeRule.mainClock.autoAdvance = false
        // the tick the green strike landed on, caught mid-effect
        show(stateAfterGreenStrikeLands())
        composeRule.mainClock.advanceTimeBy(380)

        screenshot("battle-resolve")
    }

    @Test
    fun pausedBattleOffersResume() {
        show(midBattle().copy(isPaused = true))

        composeRule.onNodeWithText("Resume").assertIsEnabled()
        screenshot("battle-paused")
    }

    @Test
    fun finishedBattleOffersARematch() {
        var rematches = 0
        show(midBattle().copy(outcome = Outcome.WON), onRematch = { rematches++ })

        composeRule.onNodeWithText("Victory").assertExists()
        screenshot("battle-won")
        composeRule.onNodeWithTag("rematch").performClick()

        assertEquals(1, rematches)
    }

    @Test
    fun finishedBattleShowsRewards() {
        val profile = FighterProfile.create("Player", Channel.RED, TechniqueRoller(Random(3)))
        val (_, rewards) = Progression.afterBattle(
            profile, List(8) { RgbColor.RED } + List(4) { RgbColor.GREEN }, won = true, TechniqueRoller(Random(3)),
        )
        show(midBattle().copy(outcome = Outcome.WON, rewards = rewards.toState()))

        composeRule.onNodeWithTag("rewards", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText("+100 XP").assertExists()
        composeRule.onNodeWithText("Level up! Level 2").assertExists()
        screenshot("battle-rewards")
    }

    @Test
    fun sixSlotArenaFitsEveryTechnique() {
        val profile = FighterProfile.create("Player", Channel.RED, TechniqueRoller(Random(3)))
        val environment = Environment(loadoutSize = 6)
        val viewModel = BattleViewModel(TICK_MS, newBattle = {
            Battle(profile.toFighter(environment), EnemyFighter(), environment)
        })
        val state = viewModel.uiState.value.also { viewModel.stop() }.copy(outcome = null)
        show(state)

        (0 until 6).forEach { composeRule.onNodeWithTag("action$it").assertIsDisplayed() }
        composeRule.onNodeWithTag("cancel").assertIsDisplayed()
        screenshot("battle-six-slots")
    }

    @Test
    fun menu() {
        val fighter = FighterProfile.create("Player", Channel.RED).toSummary()
        var loadouts = 0
        composeRule.setContent {
            BattleTheme { MainMenuScreen(fighter, onBattle = {}, onLoadout = { loadouts++ }, onMap = {}) }
        }

        composeRule.onNodeWithText("Fight the computer").assertIsEnabled()
        composeRule.onNodeWithText("Lv 1").assertExists()
        screenshot("menu")
        composeRule.onNodeWithTag("loadout").performClick()
        assertEquals(1, loadouts)
    }

    @Test
    fun loadoutMarksTheChosenTechniques() {
        val profiles = ProfileRepository(File.createTempFile("fighter", ".json").apply { delete() }) {
            val start = FighterProfile.create("Player", Channel.RED, TechniqueRoller(Random(3)), extraRolls = 4)
            // as if a few battles in, so the aura has a tint
            start.copy(aura = start.aura.gain(listOf(200 to RgbColor.BLUE)))
        }
        val viewModel = LoadoutViewModel(profiles)
        val toggled = mutableListOf<Int>()
        composeRule.setContent {
            BattleTheme {
                LoadoutScreen(viewModel.uiState.value, onToggle = { toggled += it }, onBattle = {}, onBack = {})
            }
        }

        composeRule.onNodeWithText("Bringing 4 of 4", substring = true).assertExists()
        screenshot("loadout")
        composeRule.onNodeWithTag("technique5").performClick()
        assertEquals(listOf(5), toggled)
    }

    /**
     * A battle a few ticks in, played by the real rules: the player has a strike winding
     * up and a guard queued behind it, and the computer opponent is answering.
     */
    private fun midBattle(): BattleUiState {
        val viewModel = BattleViewModel(TICK_MS)
        val scope = TestScope(dispatcher)

        viewModel.queueAction(GREEN_STRIKE)
        scope.advanceTimeBy(TICK_MS * 2 + 1)
        viewModel.queueAction(GREEN_STRIKE)
        scope.advanceTimeBy(TICK_MS + TICK_MS / 2 + 1)
        viewModel.queueAction(BLUE_GUARD)

        return viewModel.uiState.value.also { viewModel.stop() }
    }

    private fun stateAfterGreenStrikeLands(): BattleUiState {
        val viewModel = BattleViewModel(TICK_MS)
        val scope = TestScope(dispatcher)

        viewModel.queueAction(GREEN_STRIKE)
        scope.advanceTimeBy(TICK_MS * 2 + 1)

        return viewModel.uiState.value.also { viewModel.stop() }
    }

    private fun show(
        state: BattleUiState,
        onAction: (Int) -> Unit = {},
        onRematch: () -> Unit = {},
    ) {
        composeRule.setContent {
            BattleTheme {
                BattleScreen(
                    state = state,
                    tickLengthMs = TICK_MS,
                    onAction = onAction,
                    onCancel = {},
                    onTogglePause = {},
                    onGiveUp = {},
                    onRematch = onRematch,
                    onExit = {},
                )
            }
        }
        composeRule.waitForIdle()
    }

    private fun screenshot(name: String) {
        val dir = File("build/screenshots").apply { mkdirs() }
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private companion object {
        const val TICK_MS = 1000L
        const val GREEN_STRIKE = 2
        const val BLUE_GUARD = 3
    }
}
