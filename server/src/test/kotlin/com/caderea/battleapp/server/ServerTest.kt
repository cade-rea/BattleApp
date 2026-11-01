package com.caderea.battleapp.server

import com.caderea.battleapp.core.color.Channel
import com.caderea.battleapp.core.fighter.FighterProfile
import com.caderea.battleapp.core.online.ClientMessage
import com.caderea.battleapp.core.online.EndReason
import com.caderea.battleapp.core.online.MatchResult
import com.caderea.battleapp.core.online.OnlineJson
import com.caderea.battleapp.core.online.ServerMessage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Plays matches over real WebSockets against the server, with short ticks. */
class ServerTest {

    private val red = FighterProfile.create("Red", Channel.RED)
    private val blue = FighterProfile.create("Blue", Channel.BLUE)

    private fun serverTest(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        application { module(ServerConfig(tickLengthMs = TICK_MS)) }
        val client = createClient { install(WebSockets) }
        block(client)
    }

    private suspend fun HttpClient.join(profile: FighterProfile, arenaId: String = "open") =
        webSocketSession("/play").also { it.send(ClientMessage.Join(profile, arenaId)) }

    private suspend fun DefaultClientWebSocketSession.send(message: ClientMessage) =
        send(Frame.Text(OnlineJson.encode(message)))

    /** The next message, or null once the server has closed the connection. */
    private suspend fun DefaultClientWebSocketSession.next(): ServerMessage? = withTimeout(WAIT_MS) {
        while (true) {
            val frame = incoming.receiveCatching().getOrNull() ?: return@withTimeout null
            if (frame is Frame.Text) return@withTimeout OnlineJson.decodeServer(frame.readText())
        }
        @Suppress("UNREACHABLE_CODE")
        null
    }

    /** Skips ahead to the next message of type [T], such as past the states of passing ticks. */
    private suspend inline fun <reified T : ServerMessage> DefaultClientWebSocketSession.await(
        crossinline until: (T) -> Boolean = { true },
    ): T {
        while (true) {
            val message = next() ?: error("closed while waiting for ${T::class.simpleName}")
            if (message is T && until(message)) return message
        }
    }

    @Test
    fun healthCheck() = serverTest { client ->
        assertEquals("ok", client.get("/health").bodyAsText())
    }

    @Test
    fun twoPlayersInTheSameArenaAreMatched() = serverTest { client ->
        val first = client.join(red)
        assertEquals(ServerMessage.Waiting("open"), first.next())

        val second = client.join(blue)
        val firstStart = first.await<ServerMessage.MatchStarted>()
        val secondStart = second.await<ServerMessage.MatchStarted>()

        assertEquals(firstStart.matchId, secondStart.matchId)
        assertEquals("Red", firstStart.you.name)
        assertEquals("Red", secondStart.opponent.name)
        assertEquals(TICK_MS, firstStart.tickLengthMs)
        // the opening state comes right after
        assertEquals(0, first.await<ServerMessage.State>().tick)
    }

    @Test
    fun theServerRunsTheTicksAndShowsBothSides() = serverTest { client ->
        val first = client.join(red)
        val second = client.join(blue)
        first.await<ServerMessage.MatchStarted>()

        first.send(ClientMessage.Queue(RED_STRIKE))
        val telegraph = second.await<ServerMessage.State> { it.opponent.queue.isNotEmpty() }
        assertEquals(RED_STRIKE, telegraph.opponent.queue.single().slot)

        // the strike lands on a tick without either phone asking for one
        val hit = second.await<ServerMessage.State> { it.landed.isNotEmpty() }
        assertTrue(hit.tick >= 1)
        assertTrue(hit.you.health < hit.opponent.health)
    }

    @Test
    fun leavingForfeitsTheMatch() = serverTest { client ->
        val first = client.join(red)
        val second = client.join(blue)
        first.await<ServerMessage.MatchStarted>()

        second.send(ClientMessage.Leave)

        val firstOver = first.await<ServerMessage.MatchOver>()
        val secondOver = second.await<ServerMessage.MatchOver>()
        assertEquals(MatchResult.WON, firstOver.result)
        assertEquals(MatchResult.LOST, secondOver.result)
        assertEquals(EndReason.FORFEIT, firstOver.reason)
        // and the server hangs up
        assertNull(first.next())
    }

    @Test
    fun droppingOutLosesTheMatch() = serverTest { client ->
        val first = client.join(red)
        val second = client.join(blue)
        first.await<ServerMessage.MatchStarted>()

        second.close()

        val over = first.await<ServerMessage.MatchOver>()
        assertEquals(MatchResult.WON, over.result)
        assertEquals(EndReason.DISCONNECT, over.reason)
    }

    @Test
    fun playersInDifferentArenasAreNotMatched() = serverTest { client ->
        val first = client.join(red, "open")
        val second = client.join(blue, "dim")

        assertEquals(ServerMessage.Waiting("open"), first.next())
        assertEquals(ServerMessage.Waiting("dim"), second.next())
    }

    @Test
    fun leavingTheLineFreesTheSpot() = serverTest { client ->
        val quitter = client.join(red)
        assertEquals(ServerMessage.Waiting("open"), quitter.next())
        quitter.send(ClientMessage.Leave)
        assertNull(quitter.next())

        // the next player waits instead of being matched with someone who left
        val next = client.join(blue)
        assertEquals(ServerMessage.Waiting("open"), next.next())
    }

    @Test
    fun anUnknownArenaIsRejected() = serverTest { client ->
        val session = client.join(red, "nowhere")
        assertTrue(session.next() is ServerMessage.Rejected)
        assertNull(session.next())
    }

    @Test
    fun inputsBeforeJoiningAreRejected() = serverTest { client ->
        val session = client.webSocketSession("/play")
        session.send(ClientMessage.Queue(0))
        assertEquals(ServerMessage.Rejected("join first"), session.next())

        session.send(Frame.Text("not json"))
        assertEquals(ServerMessage.Rejected("unreadable message"), session.next())
    }

    @Test
    fun aQueueTheFighterCantAffordIsRejected() = serverTest { client ->
        val first = client.join(red)
        client.join(blue)
        first.await<ServerMessage.MatchStarted>()

        first.send(ClientMessage.Queue(42))
        assertTrue(first.await<ServerMessage.Rejected>().reason.contains("42"))
    }

    private companion object {
        const val TICK_MS = 50L
        const val WAIT_MS = 5_000L
        const val RED_STRIKE = 0
    }
}
