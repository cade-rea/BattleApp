package com.caderea.battleapp.core.progression

import com.caderea.battleapp.core.Battle
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.fighter.Fighter
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.technique.TechniqueRoller
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProgressionTest {

    private val roller = TechniqueRoller(Random(5))
    private val profile = FighterProfile.create("Cade", Channel.RED, roller)

    @Test
    fun eachTechniqueUsedEarnsTheSameXpWhateverItsColor() {
        val (updated, rewards) = Progression.afterBattle(
            profile, listOf(RgbColor.RED, RgbColor.BLUE, RgbColor.GREEN), won = false, roller,
        )

        assertEquals(3 * Progression.XP_PER_TECHNIQUE, rewards.xp)
        assertEquals(profile.aura.totalXp + rewards.xp, updated.aura.totalXp)
        assertTrue(updated.aura.blue > 0 && updated.aura.green > 0)
        assertEquals(1, updated.losses)
        assertNull(rewards.newTechnique)
        assertEquals(profile.catalog, updated.catalog)
    }

    @Test
    fun aWinAddsABonusAndANewTechnique() {
        val (updated, rewards) = Progression.afterBattle(profile, listOf(RgbColor.RED), won = true, roller)

        assertEquals(Progression.XP_PER_TECHNIQUE + Progression.WIN_BONUS, rewards.xp)
        val technique = assertNotNull(rewards.newTechnique)
        assertEquals(technique, updated.catalog.last())
        assertEquals(1, updated.wins)
        // a win with only red keeps the aura pure red
        assertEquals(0, updated.aura.green + updated.aura.blue)
        assertEquals(1, rewards.levelsGained)
    }

    @Test
    fun aBattleCountsWhatThePlayerLanded() {
        val player = profile.toFighter()
        val enemy = Fighter(name = "Dummy", color = RgbColor.GREEN, maxHealth = 1, actions = listOf())
        val battle = Battle(player, enemy, opponent = null)

        battle.queueAction(0) // red strike: 1 tick, and the dummy has 1 health
        battle.tick()

        assertEquals(player, battle.winner)
        val (_, rewards) = Progression.afterBattle(profile, battle, roller)
        assertEquals(Progression.XP_PER_TECHNIQUE + Progression.WIN_BONUS, rewards.xp)
        assertTrue(rewards.won)
    }

    @Test
    fun aStoppedBattleHasNoWinner() {
        val battle = Battle(opponent = null)
        battle.stop()

        assertNull(battle.winner)
        assertTrue(!Progression.afterBattle(profile, battle, roller).second.won)
    }
}
