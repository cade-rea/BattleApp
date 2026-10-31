package com.caderea.battleapp.data

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.fighter.ProfileJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ProfileRepositoryTest {

    private val root = Files.createTempDirectory("files").toFile()
    private val directory = File(root, "characters")

    private fun repository() = ProfileRepository(directory, legacyFile = File(root, "fighter.json"))

    @Test
    fun aFirstLaunchHasNoCharacters() {
        val profiles = repository()

        assertEquals(emptyList<SavedProfile>(), profiles.roster.value)
        assertNull(profiles.active.value)
    }

    @Test
    fun addingACharacterLoadsIt() {
        val profiles = repository()
        profiles.add(FighterProfile.create("Ada", Channel.RED))
        val id = profiles.add(FighterProfile.create("Bo", Channel.BLUE))

        assertEquals(listOf("Ada", "Bo"), profiles.roster.value.map { it.profile.name })
        assertEquals(id, profiles.activeId.value)
        assertEquals("Bo", profiles.active.value?.name)
    }

    @Test
    fun charactersTheLoadedOneAndChangesSurviveARestart() {
        val first = repository()
        val ada = first.add(FighterProfile.create("Ada", Channel.RED))
        first.add(FighterProfile.create("Bo", Channel.BLUE))
        first.add(FighterProfile.create("Cy", Channel.GREEN))
        first.select(ada)
        first.update { it.copy(wins = 7).withLoadout(listOf(2, 1)) }

        val reloaded = repository()
        assertEquals(listOf("Ada", "Bo", "Cy"), reloaded.roster.value.map { it.profile.name })
        assertEquals("Ada", reloaded.active.value?.name)
        assertEquals(7, reloaded.requireActive().wins)
        assertEquals(listOf(2, 1), reloaded.requireActive().loadout)
        assertEquals(0, reloaded.roster.value.first { it.profile.name == "Bo" }.profile.wins)
    }

    @Test
    fun deletingTheLoadedCharacterLoadsTheOldestLeft() {
        val profiles = repository()
        val ada = profiles.add(FighterProfile.create("Ada", Channel.RED))
        val bo = profiles.add(FighterProfile.create("Bo", Channel.BLUE))

        profiles.delete(bo)
        assertEquals("Ada", profiles.active.value?.name)

        profiles.delete(ada)
        assertNull(profiles.active.value)
        assertEquals(emptyList<SavedProfile>(), repository().roster.value)
    }

    @Test
    fun theOldSingleFighterMovesInAsTheFirstCharacter() {
        val legacy = File(root, "fighter.json")
        legacy.writeText(ProfileJson.encode(FighterProfile.create("Veteran", Channel.GREEN).copy(wins = 12)))

        val profiles = repository()

        assertEquals("Veteran", profiles.active.value?.name)
        assertEquals(12, profiles.requireActive().wins)
        assertFalse(legacy.exists())
        assertEquals("Veteran", repository().active.value?.name)
    }

    @Test
    fun aDamagedFileOnlyLosesThatCharacter() {
        val first = repository()
        first.add(FighterProfile.create("Ada", Channel.RED))
        val bo = first.add(FighterProfile.create("Bo", Channel.BLUE))
        File(directory, "$bo.json").writeText("{ not json")

        val reloaded = repository()

        assertEquals(listOf("Ada"), reloaded.roster.value.map { it.profile.name })
        assertEquals("Ada", reloaded.active.value?.name)
    }
}
