package com.caderea.battleapp.core.progression

import com.caderea.battleapp.core.color.RgbColor
import kotlin.test.Test
import kotlin.test.assertEquals

class LevelsTest {

    @Test
    fun eachLevelTakesMoreXpThanTheLast() {
        assertEquals(60, Levels.xpFor(1))
        assertEquals(100, Levels.xpFor(2))
        assertEquals(180, Levels.xpFor(3))
        assertEquals(1860, Levels.xpFor(10))
    }

    @Test
    fun levelForIsTheInverseOfXpFor() {
        for (level in 1..Levels.MAX_LEVEL) {
            assertEquals(level, Levels.levelFor(Levels.xpFor(level)), "at level $level")
            if (level > 1) assertEquals(level - 1, Levels.levelFor(Levels.xpFor(level) - 1), "just under $level")
        }
        assertEquals(1, Levels.levelFor(0))
        assertEquals(Levels.MAX_LEVEL, Levels.levelFor(Int.MAX_VALUE / 2))
    }

    @Test
    fun theBudgetGrowsWithLevel() {
        assertEquals(RgbColor.STARTING_POWER_BUDGET, Levels.powerBudget(1))
        assertEquals(345, Levels.powerBudget(10))
    }

    @Test
    fun progressRunsFromZeroToOneWithinALevel() {
        assertEquals(0.0, Levels.progress(100))
        assertEquals(0.5, Levels.progress(140))
        assertEquals(1.0, Levels.progress(Levels.xpFor(Levels.MAX_LEVEL)))
    }
}
