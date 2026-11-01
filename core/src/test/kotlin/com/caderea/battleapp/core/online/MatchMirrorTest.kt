package com.caderea.battleapp.core.online

import com.caderea.battleapp.core.action.Guard
import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.environment.Arenas
import com.caderea.battleapp.core.fighter.FighterProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MatchMirrorTest {

    // red's starter kit: Red Strike, Green Mend, Green Strike, Blue Guard
    private val red = FighterProfile.create("Red", Channel.RED)
    private val blue = FighterProfile.create("Blue", Channel.BLUE)

    private fun info(profile: FighterProfile) = FighterInfo(
        name = profile.name,
        color = profile.aura.color,
        level = profile.level,
        maxHealth = 2000,
        techniques = profile.loadoutFor(4),
    )

    private val mirror = MatchMirror(
        ServerMessage.MatchStarted("m", Arenas.TURNING.id, 1000, info(red), info(blue))
    )

    @Test
    fun theFightersComeFromTheMatchStart() {
        assertEquals("Red", mirror.you.name)
        assertEquals("Blue", mirror.opponent.name)
        assertSame(mirror.opponent, mirror.you.target)
        assertEquals(listOf("Red Strike", "Green Mend", "Green Strike", "Blue Guard"), mirror.you.actions.map { it?.name })
        assertSame(Arenas.TURNING, mirror.arena)
    }

    @Test
    fun aStateIsCopiedOntoTheFighters() {
        mirror.apply(
            ServerMessage.State(
                tick = 5,
                you = FighterState(1200, 3, listOf(QueuedTechnique(2, 7, 1), QueuedTechnique(0, 8, 1)), GuardUp(3, 0.4, 2)),
                opponent = FighterState(900, 30, emptyList()),
                landed = listOf(LandedAction(Side.OPPONENT, 0)),
            )
        )

        assertEquals(5, mirror.tick)
        assertEquals(1200, mirror.you.health)
        assertEquals(900, mirror.opponent.health)
        assertEquals(listOf(7, 8), mirror.you.queue.map { it.id })
        assertEquals("Green Strike", mirror.you.queue.peek()!!.action.name)
        assertEquals(1, mirror.you.queue.peek()!!.ticksLeft)
        assertTrue(mirror.you.activeGuard!!.guard is Guard)
        assertEquals(0.4, mirror.you.activeGuard!!.strength)
        assertNull(mirror.opponent.activeGuard)

        val landing = mirror.landed.single()
        assertSame(mirror.opponent, landing.performer)
        assertSame(mirror.opponent.actions[0], landing.action)
        // the previews look at the light of the next tick
        assertEquals(Arenas.TURNING.lightAt(6), mirror.conditions.lights.single())
    }

    @Test
    fun theFightersQueueOnlyWhatTheServerSays() {
        // the mirror's own rules agree with the server's on what can be queued
        mirror.apply(ServerMessage.State(0, FighterState(2000, 5, emptyList()), FighterState(2000, 20, emptyList())))
        assertFalse(mirror.you.canQueue(0))
        assertTrue(mirror.opponent.canQueue(0))
    }
}
