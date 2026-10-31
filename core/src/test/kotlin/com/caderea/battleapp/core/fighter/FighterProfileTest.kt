package com.caderea.battleapp.core.fighter

import com.caderea.battleapp.core.action.Attack
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.environment.Environment
import com.caderea.battleapp.core.progression.Aura
import com.caderea.battleapp.core.progression.Levels
import com.caderea.battleapp.core.technique.Technique
import com.caderea.battleapp.core.technique.TechniqueKind
import com.caderea.battleapp.core.technique.TechniqueRoller
import com.caderea.battleapp.core.technique.TechniqueRolls
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FighterProfileTest {

    private val roller = TechniqueRoller(Random(11))

    @Test
    fun aNewRedFighterGetsTheClassicStarterKit() {
        val profile = FighterProfile.create("Cade", Channel.RED, roller)

        assertEquals(1, profile.level)
        assertEquals(
            listOf("Red Strike", "Green Mend", "Green Strike", "Blue Guard"),
            profile.loadoutFor(4).map { it.name },
        )
        // plus two rolled techniques to choose from
        assertEquals(6, profile.catalog.size)
    }

    @Test
    fun starterKitsFollowTheTypeCycle() {
        val green = FighterProfile.starterKit(Channel.GREEN).map { it.name }
        val blue = FighterProfile.starterKit(Channel.BLUE).map { it.name }

        assertEquals(listOf("Green Strike", "Green Mend", "Blue Strike", "Red Guard"), green)
        assertEquals(listOf("Blue Strike", "Green Mend", "Red Strike", "Green Guard"), blue)
    }

    @Test
    fun theArenaSetsHowManyTechniquesComeAlong() {
        val profile = FighterProfile.create("Cade", Channel.RED, roller)

        assertEquals(4, profile.toFighter(Environment(loadoutSize = 4)).actions.size)
        val six = profile.toFighter(Environment(loadoutSize = 6)).actions
        // the loadout holds four, so the rest of the catalog tops it up
        assertEquals(6, six.filterNotNull().size)
    }

    @Test
    fun slotsTheCatalogCantFillStayEmpty() {
        val small = FighterProfile("Solo", Aura.start(Channel.RED), FighterProfile.starterKit(Channel.RED))

        val actions = small.toFighter(Environment(loadoutSize = 6)).actions
        assertEquals(6, actions.size)
        assertEquals(2, actions.count { it == null })
    }

    @Test
    fun theLoadoutPicksTechniquesInButtonOrder() {
        val profile = FighterProfile.create("Cade", Channel.RED, roller).withLoadout(listOf(6, 1, 4))

        assertEquals(listOf(6, 1, 4, 2), profile.loadoutFor(4).map { it.id })
        assertFailsWith<IllegalArgumentException> { profile.withLoadout(listOf(99)) }
        assertFailsWith<IllegalArgumentException> { profile.withLoadout(listOf(1, 1)) }
        assertFailsWith<IllegalArgumentException> { profile.withLoadout(listOf(1, 2, 3, 4, 5, 6, 7)) }
    }

    @Test
    fun aLevelCapDimsTechniquesToThatLevelsBudget() {
        val bright = Technique(1, TechniqueKind.ATTACK, "Yellow Strike", RgbColor(255, 200, 0))
        val veteran = FighterProfile("Vet", Aura(Channel.RED, red = Levels.xpFor(30)), listOf(bright))

        val uncapped = veteran.toFighter(Environment()).actions[0]!!
        val capped = veteran.toFighter(Environment(levelCap = 1)).actions[0]!!

        assertEquals(455, uncapped.color.power)
        assertTrue(capped.color.power <= Levels.powerBudget(1))
        assertTrue(capped is Attack)
    }

    @Test
    fun learningAddsToTheCatalogWithoutTouchingTheLoadout() {
        val profile = FighterProfile.create("Cade", Channel.RED, roller)
        val learned = profile.learn(roller.roll(profile.aura, profile.nextTechniqueId))

        assertEquals(7, learned.catalog.size)
        assertEquals(profile.loadout, learned.loadout)
        assertNull(profile.technique(7))
    }

    @Test
    fun autoLoadoutFillsTheStarterRolesWithTheStrongestFit() {
        val strongerRed = Technique(9, TechniqueKind.ATTACK, "Red Strike", RgbColor.RED, TechniqueRolls(potency = 1.1))
        val profile = FighterProfile(
            "AI", Aura.start(Channel.RED), FighterProfile.starterKit(Channel.RED) + strongerRed,
        ).withAutoLoadout()

        assertEquals(9, profile.loadout.first())
        assertEquals(listOf(9, 2, 3, 4, 1), profile.loadout)
    }

    @Test
    fun profilesSurviveASaveAndLoad() {
        val profile = FighterProfile.create("Cade", Channel.BLUE, roller).copy(wins = 3)

        assertEquals(profile, ProfileJson.decode(ProfileJson.encode(profile)))
    }

    @Test
    fun opponentsMatchTheRequestedLevel() {
        for (level in listOf(1, 5, 20)) {
            val enemy = Opponents.atLevel(level, Random(level))
            assertEquals(level, enemy.level)
            assertEquals(Environment.MAX_LOADOUT, enemy.loadout.size)
            assertTrue(enemy.toFighter().actions.all { it != null })
        }
    }
}
