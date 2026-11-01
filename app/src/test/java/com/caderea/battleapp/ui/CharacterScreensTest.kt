package com.caderea.battleapp.ui

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.fighter.CharacterCreator
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.technique.TechniqueRoller
import com.caderea.battleapp.data.emptyRepository
import com.caderea.battleapp.ui.character.CharacterCreationScreen
import com.caderea.battleapp.ui.character.CharactersScreen
import com.caderea.battleapp.ui.menu.MainMenuScreen
import com.caderea.battleapp.ui.theme.BattleTheme
import com.caderea.battleapp.viewmodel.ActionKind
import com.caderea.battleapp.viewmodel.CharacterCreationViewModel
import com.caderea.battleapp.viewmodel.CharactersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import kotlin.random.Random

/** Character creation and the character list, with screenshots in app/build/screenshots. */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w400dp-h880dp-xhdpi")
class CharacterScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun creatingACharacterFromNameToSignatureMove() {
        val profiles = emptyRepository()
        val viewModel = CharacterCreationViewModel(profiles, CharacterCreator(TechniqueRoller(Random(21))))
        var done = 0
        composeRule.setContent {
            BattleTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CharacterCreationScreen(
                    state,
                    onName = viewModel::setName,
                    onBase = viewModel::setBase,
                    onStart = viewModel::startDraft,
                    onChooseSignature = viewModel::chooseSignature,
                    onToggle = viewModel::toggle,
                    onKeep = viewModel::keep,
                    onStartOver = viewModel::startOver,
                    onDone = { done++ },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithTag("start").assertIsNotEnabled()
        composeRule.onNodeWithTag("name").performTextInput("Nova")
        composeRule.onNodeWithTag("baseBLUE").performClick()
        composeRule.onNodeWithTag("start").assertIsEnabled()
        screenshot("create-identity")

        composeRule.onNodeWithTag("start").performClick()
        composeRule.onNodeWithText("Signature move").assertExists()
        screenshot("create-signature")

        composeRule.onNodeWithTag("offer0").performClick()
        composeRule.onNodeWithText("Pick 2 of 6").assertExists()
        composeRule.onNodeWithTag("keep").assertIsNotEnabled()
        val state = viewModel.uiState.value
        val needsAttack = state.kept.none { it.kind == ActionKind.ATTACK }
        val first = if (needsAttack) state.offer.indexOfFirst { it.kind == ActionKind.ATTACK } else 0
        val second = state.offer.indices.first { it != first }
        composeRule.onNodeWithTag("offer$first").performClick()
        composeRule.onNodeWithTag("offer$second").performClick()
        composeRule.onNodeWithTag("keep").assertIsEnabled()
        screenshot("create-techniques")

        assertNull(profiles.active.value)
        composeRule.onNodeWithTag("keep").performClick()
        composeRule.onNodeWithText("is ready to fight").assertExists()
        screenshot("create-ready")

        assertEquals("Nova", profiles.requireActive().name)
        assertEquals(3, profiles.requireActive().catalog.size)
        composeRule.onNodeWithTag("done").performClick()
        assertEquals(1, done)
    }

    @Test
    fun characterListShowsWhoIsLoaded() {
        val profiles = emptyRepository()
        val ada = profiles.add(FighterProfile.create("Ada", Channel.RED).copy(wins = 4, losses = 1))
        profiles.add(FighterProfile.create("Bo", Channel.BLUE).let { it.copy(aura = it.aura.gain(listOf(300 to RgbColor.GREEN))) })
        profiles.add(FighterProfile.create("Cy", Channel.GREEN))
        profiles.select(ada)
        val viewModel = CharactersViewModel(profiles)
        composeRule.setContent {
            BattleTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                CharactersScreen(state, onSelect = viewModel::select, onDelete = viewModel::delete, onNew = {}, onBack = {})
            }
        }

        composeRule.onNodeWithText("Loaded").assertExists()
        screenshot("characters")

        composeRule.onNodeWithTag("characterBo").performClick()
        assertEquals("Bo", profiles.requireActive().name)

        composeRule.onNodeWithTag("deleteCy").performClick()
        screenshot("characters-delete")
        composeRule.onNodeWithTag("confirmDelete").performClick()
        assertEquals(listOf("Ada", "Bo"), profiles.roster.value.map { it.profile.name })
    }

    @Test
    fun menuWithNoCharacterOffersToMakeOne() {
        var creates = 0
        composeRule.setContent {
            BattleTheme {
                MainMenuScreen(null, onBattle = {}, onOnline = {}, onLoadout = {}, onCharacters = {}, onCreate = { creates++ })
            }
        }

        composeRule.onNodeWithText("Fight the computer").assertDoesNotExist()
        screenshot("menu-empty")
        composeRule.onNodeWithTag("create").performClick()
        assertEquals(1, creates)
    }

    private fun screenshot(name: String) {
        val dir = File("build/screenshots").apply { mkdirs() }
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
