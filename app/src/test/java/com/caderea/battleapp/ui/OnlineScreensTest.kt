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
import androidx.compose.ui.test.performTextReplacement
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.FighterInfo
import com.caderea.battleapp.core.online.FighterState
import com.caderea.battleapp.core.online.MatchResult
import com.caderea.battleapp.core.online.QueuedTechnique
import com.caderea.battleapp.core.online.ServerMessage
import com.caderea.battleapp.online.FakeConnector
import com.caderea.battleapp.online.ServerAddress
import com.caderea.battleapp.ui.online.OnlineBattleScreen
import com.caderea.battleapp.ui.online.OnlineSetupScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.OnlineBattleViewModel
import com.caderea.battleapp.viewmodel.OnlineUiState
import com.caderea.battleapp.viewmodel.toSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
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

/** The online screens, with screenshots in app/build/screenshots/online-*.png. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w400dp-h880dp-xhdpi")
class OnlineScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val dispatcher = StandardTestDispatcher()
    private val red = FighterProfile.create("Ash", Channel.RED)
    private val blue = FighterProfile.create("Rook", Channel.BLUE)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun theSetupFillsInTheAddress() {
        var picked: String? = null
        composeRule.setContent {
            BattleTheme { OnlineSetupScreen(red.toSummary(), ServerAddress.DEFAULT, onContinue = { picked = it }, onBack = {}) }
        }

        composeRule.onNodeWithTag("server").performTextReplacement("192.168.1.20")
        screenshot("online-setup")
        composeRule.onNodeWithTag("continue").performClick()
        assertEquals("ws://192.168.1.20:8080/play", picked)

        composeRule.onNodeWithTag("server").performTextReplacement("not an address")
        composeRule.onNodeWithTag("continue").assertIsNotEnabled()
    }

    @Test
    fun waitingForAnOpponent() {
        val (viewModel, connector) = viewModel()
        connector.last.receive(ServerMessage.Waiting("open"))
        settle()

        show(viewModel.uiState.value)
        composeRule.onNodeWithText("Looking for an opponent in Red Glow…").assertExists()
        composeRule.onNodeWithTag("stop_looking").assertIsEnabled()
        screenshot("online-waiting")
    }

    @Test
    fun anOnlineBattleHasNoPause() {
        val (viewModel, connector) = viewModel()
        connector.last.receive(started())
        connector.last.receive(
            ServerMessage.State(
                tick = 4,
                you = FighterState(1620, 15, listOf(QueuedTechnique(2, 3, 2))),
                opponent = FighterState(1410, 10, listOf(QueuedTechnique(0, 5, 3))),
            )
        )
        settle()

        var tapped = -1
        show(viewModel.uiState.value, onAction = { tapped = it })
        composeRule.onNodeWithTag("pause").assertDoesNotExist()
        composeRule.onNodeWithTag("action0").performClick()
        assertEquals(0, tapped)
        screenshot("online-battle")
    }

    @Test
    fun theOutcomeSaysWhyItEnded() {
        val (viewModel, connector) = viewModel()
        connector.last.receive(started())
        connector.last.receive(ServerMessage.MatchOver(MatchResult.WON, EndReason.FORFEIT, 12))
        settle()

        show(viewModel.uiState.value)
        composeRule.onNodeWithText("Your opponent gave up.").assertExists()
        composeRule.onNodeWithText("Find another match").assertExists()
        screenshot("online-won")
    }

    @Test
    fun aFailureOffersToTryAgain() {
        var again = 0
        show(
            OnlineUiState(phase = com.caderea.battleapp.viewmodel.OnlinePhase.FAILED, problem = "Failed to connect to /10.0.2.2:8080"),
            onAgain = { again++ },
        )
        composeRule.onNodeWithText("Couldn't play online").assertExists()
        screenshot("online-failed")
        composeRule.onNodeWithTag("try_again").performClick()
        assertEquals(1, again)
    }

    private fun viewModel(): Pair<OnlineBattleViewModel, FakeConnector> {
        val connector = FakeConnector()
        val viewModel = OnlineBattleViewModel(connector, ServerAddress.DEFAULT, red, Arenas.RED_GLOW)
        settle()
        return viewModel to connector
    }

    private fun started() = ServerMessage.MatchStarted(
        "m1", Arenas.RED_GLOW.id, 1000,
        FighterInfo(red.name, red.aura.color, 1, 2000, red.loadoutFor(5)),
        FighterInfo(blue.name, blue.aura.color, 1, 2000, blue.loadoutFor(5)),
    )

    private fun settle() = TestScope(dispatcher).runCurrent()

    private fun show(state: OnlineUiState, onAction: (Int) -> Unit = {}, onAgain: () -> Unit = {}) {
        composeRule.setContent {
            BattleTheme {
                OnlineBattleScreen(state, onAction = onAction, onCancel = {}, onGiveUp = {}, onAgain = onAgain, onExit = {})
            }
        }
        composeRule.waitForIdle()
    }

    private fun screenshot(name: String) {
        val dir = File("build/screenshots").apply { mkdirs() }
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
