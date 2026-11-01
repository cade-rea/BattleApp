package com.caderea.battleapp.core.online

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.color.RgbColor
import com.caderea.battleapp.core.fighter.FighterProfile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProtocolTest {

    @Test
    fun clientMessagesSurviveTheTrip() {
        val messages = listOf(
            ClientMessage.Join(FighterProfile.create("Ash", Channel.RED), "turning"),
            ClientMessage.Queue(2),
            ClientMessage.Cancel,
            ClientMessage.Leave,
        )
        for (message in messages) {
            assertEquals(message, OnlineJson.decodeClient(OnlineJson.encode(message)))
        }
    }

    @Test
    fun serverMessagesSurviveTheTrip() {
        val fighter = FighterState(
            health = 1500,
            energy = 12,
            queue = listOf(QueuedTechnique(slot = 1, id = 4, ticksLeft = 2)),
            guard = GuardUp(slot = 3, strength = 0.42, ticksLeft = 1),
        )
        val profile = FighterProfile.create("Ash", Channel.BLUE)
        val info = FighterInfo("Ash", RgbColor.BLUE, 1, 2000, profile.catalog.take(3) + null)
        val messages = listOf(
            ServerMessage.Waiting("open"),
            ServerMessage.MatchStarted("m1", "open", 1000, info, info),
            ServerMessage.State(7, fighter, fighter.copy(guard = null), listOf(LandedAction(Side.OPPONENT, 0))),
            ServerMessage.MatchOver(MatchResult.WON, EndReason.KNOCKOUT, 31),
            ServerMessage.Rejected("join first"),
        )
        for (message in messages) {
            assertEquals(message, OnlineJson.decodeServer(OnlineJson.encode(message)))
        }
    }

    @Test
    fun messagesAreTaggedWithTheirType() {
        assertEquals("""{"type":"queue","slot":0}""", OnlineJson.encode(ClientMessage.Queue(0)))
        assertEquals("""{"type":"cancel"}""", OnlineJson.encode(ClientMessage.Cancel))
    }

    @Test
    fun unknownFieldsAreSkipped() {
        // so a newer server can add fields without breaking older apps
        val message = OnlineJson.decodeServer("""{"type":"waiting","arenaId":"open","queuedPlayers":3}""")
        assertTrue(message is ServerMessage.Waiting)
    }
}
