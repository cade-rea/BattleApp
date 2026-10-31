package com.caderea.battleapp.data

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProfileRepositoryTest {

    private val file = File.createTempFile("fighter", ".json").apply { delete() }

    @Test
    fun aFirstLaunchMakesAndSavesADefaultFighter() {
        val profile = ProfileRepository(file).profile.value

        assertEquals(ProfileRepository.DEFAULT_NAME, profile.name)
        assertEquals(Channel.RED, profile.aura.base)
        assertTrue(file.exists())
    }

    @Test
    fun updatesSurviveARestart() {
        ProfileRepository(file).update { it.copy(wins = 7).withLoadout(listOf(2, 1)) }

        val reloaded = ProfileRepository(file).profile.value
        assertEquals(7, reloaded.wins)
        assertEquals(listOf(2, 1), reloaded.loadout)
    }

    @Test
    fun aDamagedFileStartsOver() {
        file.writeText("{ not json")

        val profile = ProfileRepository(file) { FighterProfile.create("Fresh", Channel.BLUE) }.profile.value

        assertEquals("Fresh", profile.name)
    }
}
